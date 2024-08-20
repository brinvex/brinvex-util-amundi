/*
 * Copyright © 2023 Brinvex (dev@brinvex.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brinvex.util.amundi.impl;

import com.brinvex.util.amundi.api.model.statement.Trade;
import com.brinvex.util.amundi.api.model.statement.TradeType;
import com.brinvex.util.amundi.api.service.AmndParser;
import com.brinvex.util.amundi.api.service.exception.AmndException;
import com.brinvex.util.amundi.impl.builder.TradeBuilder;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class AmndParserImpl implements AmndParser {

    private final PdfReader pdfReader = new PdfReader();

    private static class Lazy {
        static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{8,}");
        static final Pattern MONEY_PATTERN = Pattern.compile("-?(\\d+\\s)*\\d+,\\d{2}");
        static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        static final Pattern ISIN_PATTERN = Pattern.compile("([A-Z]{2}[A-Z\\d]{9}\\d)");

    }

    @SuppressWarnings({"CaughtExceptionImmediatelyRethrown", "unused", "UnusedAssignment"})
    @Override
    public List<Trade> parseTransactionStatement(InputStream statementContent) {
        List<String> lines = pdfReader.readPdfLines(statementContent);

        String accNumber = lines.get(1).trim();
        assertLine(2, accNumber, Lazy.ACCOUNT_NUMBER_PATTERN);

        int tranIdx = (int) (
                lines.stream()
                        .skip(2)
                        .map(String::trim)
                        .takeWhile(not("Výpis operácií z registra podielnikov"::equals))
                        .count() + 3);

        String line = lines.get(tranIdx);
        assertLine(tranIdx, line, "Dátum a čas");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "zobchodovania");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "Dátum a čas");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "vysporiadani");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "a");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "Dátum a čas");
        line = lines.get(++tranIdx);
        assertLine(tranIdx, line, "pokynu");

        lines = lines.stream().filter(s -> s.length() < 120).toList();

        List<Trade> trades = new ArrayList<>();
        for (int i = tranIdx + 1; i < lines.size(); i++) {

            TradeType tradeType;
            LocalDate orderDate;
            LocalTime tradeTime;
            LocalDate settleDate;
            BigDecimal fees;
            BigDecimal netAmount1;
            BigDecimal netAmount2;
            LocalDate tradeDate;
            BigDecimal qty;
            BigDecimal unitPrice;
            LocalDate priceDate;
            String instName;
            String isin;
            String desc1;
            String desc2;

            try {
                desc1 = lines.get(i);
                desc2 = lines.get(++i);
                if (desc1.startsWith("Investícia")) {
                    line = lines.get(++i);
                    assertLine(i, line, "Bezhotovostný prevod");
                    tradeType = TradeType.BUY;
                } else if (desc1.startsWith("Spätné odkúpenie")) {
                    tradeType = TradeType.SELL;
                } else {
                    break;
                }

                {
                    String[] parts = lines.get(++i).trim().split("EUR");
                    assertLine(i, parts[0].trim(), Lazy.MONEY_PATTERN);
                    fees = Util.parseDecimal(parts[0]);
                    netAmount1 = Util.parseDecimal(parts[1]);
                    netAmount2 = Util.parseDecimal(parts[2]);
                    if (netAmount1.compareTo(BigDecimal.ZERO) != 0
                        && netAmount1.setScale(0, RoundingMode.DOWN).compareTo(netAmount2.setScale(0, RoundingMode.DOWN)) != 0) {
                        String message = "Unexpected %s.line: %s %s".formatted(i + 1, netAmount1, netAmount2);
                        throw new AmndException(message);
                    }
                    tradeDate = LocalDate.parse(parts[3].trim(), Lazy.DF);
                }

                line = lines.get(++i);
                tradeTime = LocalTime.parse(line);

                line = lines.get(++i);
                settleDate = LocalDate.parse(line, Lazy.DF);

                line = lines.get(++i);
                assertLine(i, line, "00:00:00");

                line = lines.get(++i);
                orderDate = LocalDate.parse(line, Lazy.DF);

                line = lines.get(++i);

                {
                    line = lines.get(++i);
                    int n1 = line.indexOf(' ');
                    String part0 = line.substring(0, n1);
                    unitPrice = Util.parseDecimal(part0);

                    String part1 = line.substring(n1 + 1, n1 + 4);
                    assertLine(i, part1, "EUR");

                    int n2 = line.lastIndexOf(' ');
                    qty = Util.parseDecimal(line.substring(n2));

                    String innerStr = line.substring(n1 + 4, n2);
                    int n3 = innerStr.lastIndexOf(' ');
                    instName = innerStr.substring(0, n3);
                    priceDate = LocalDate.parse(innerStr.substring(n3 + 1, n3 + 11), Lazy.DF);
                }

                isin = lines.get(++i);
                assertLine(i, isin, Lazy.ISIN_PATTERN);

            } catch (Exception e) {
                throw e;
            }

            BigDecimal tranNetAmount = netAmount2.negate();
            trades.add(new TradeBuilder()
                    .accountId(accNumber)
                    .type(tradeType)
                    .orderDate(orderDate)
                    .tradeDate(tradeDate)
                    .settleDate(settleDate)
                    .isin(isin)
                    .instrumentName(instName)
                    .description("%s %s".formatted(desc1, desc2).trim())
                    .ccy("EUR")
                    .fees(fees.negate())
                    .netValue(tranNetAmount)
                    .quantity(qty)
                    .price(unitPrice)
                    .priceDate(priceDate)
                    .build());
        }
        return trades;
    }

    private void assertLine(int lineIdxZeroBased, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            String message = "Unexpected %s.line: '%s'".formatted(lineIdxZeroBased + 1, actual);
            throw new AmndException(message);
        }
    }

    private void assertLine(int lineIdxZeroBased, String actual, Pattern expectedPattern) {
        if (actual == null || !expectedPattern.matcher(actual).matches()) {
            String message = "Unexpected %s.line: '%s'".formatted(lineIdxZeroBased + 1, actual);
            throw new AmndException(message);
        }
    }
}
