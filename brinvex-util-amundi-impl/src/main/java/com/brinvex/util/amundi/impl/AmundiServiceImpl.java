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

import com.brinvex.util.amundi.api.model.Currency;
import com.brinvex.util.amundi.api.model.Transaction;
import com.brinvex.util.amundi.api.model.TransactionType;
import com.brinvex.util.amundi.api.service.AmundiService;
import com.brinvex.util.amundi.api.service.exception.InvalidStatementException;

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

@SuppressWarnings("SpellCheckingInspection")
public class AmundiServiceImpl implements AmundiService {

    private final PdfReader pdfReader = new PdfReader();

    private static class LazyHolder {
        static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{8,}");
        static final Pattern MONEY_PATTERN = Pattern.compile("-?(\\d+\\s)*\\d+,\\d{2}");
        static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        static final Pattern ISIN_PATTERN = Pattern.compile("([A-Z]{2}[A-Z\\d]{9}\\d)");

    }

    @SuppressWarnings({"CaughtExceptionImmediatelyRethrown", "unused", "UnusedAssignment"})
    @Override
    public List<Transaction> parseTransactionStatements(InputStream statementInputStream) {
        List<String> lines = pdfReader.readPdfLines(statementInputStream);

        String accNumber = lines.get(1).trim();
        assertLine(2, accNumber, LazyHolder.ACCOUNT_NUMBER_PATTERN);

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

        List<Transaction> trans = new ArrayList<>();
        for (int i = tranIdx + 1; i < lines.size(); i++) {

            TransactionType tranType;
            LocalDate orderDay;
            LocalTime tradeTime;
            LocalDate settleDay;
            BigDecimal fees;
            BigDecimal grossAmount1;
            BigDecimal grossAmount2;
            LocalDate tradeDay;
            BigDecimal qty;
            BigDecimal unitPrice;
            LocalDate priceDay;
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
                    tranType = TransactionType.BUY;
                } else if (desc1.startsWith("Spätné odkúpenie")) {
                    tranType = TransactionType.SELL;
                } else {
                    break;
                }

                {
                    String[] parts = lines.get(++i).trim().split("EUR");
                    assertLine(i, parts[0].trim(), LazyHolder.MONEY_PATTERN);
                    fees = ParseUtil.parseDecimal(parts[0]);
                    grossAmount1 = ParseUtil.parseDecimal(parts[1]);
                    grossAmount2 = ParseUtil.parseDecimal(parts[2]);
                    if (grossAmount1.compareTo(BigDecimal.ZERO) != 0
                        && grossAmount1.setScale(0, RoundingMode.DOWN).compareTo(grossAmount2.setScale(0, RoundingMode.DOWN)) != 0) {
                        throw new InvalidStatementException("Unexpected %s.line: %s %s".formatted(i + 1, grossAmount1, grossAmount2));
                    }
                    tradeDay = LocalDate.parse(parts[3].trim(), LazyHolder.DF);
                }

                line = lines.get(++i);
                tradeTime = LocalTime.parse(line);

                line = lines.get(++i);
                settleDay = LocalDate.parse(line, LazyHolder.DF);

                line = lines.get(++i);
                assertLine(i, line, "00:00:00");

                line = lines.get(++i);
                orderDay = LocalDate.parse(line, LazyHolder.DF);

                line = lines.get(++i);

                {
                    line = lines.get(++i);
                    int n1 = line.indexOf(' ');
                    String part0 = line.substring(0, n1);
                    unitPrice = ParseUtil.parseDecimal(part0);

                    String part1 = line.substring(n1 + 1, n1 + 4);
                    assertLine(i, part1, "EUR");

                    int n2 = line.lastIndexOf(' ');
                    qty = ParseUtil.parseDecimal(line.substring(n2));

                    String innerStr = line.substring(n1 + 4, n2);
                    int n3 = innerStr.lastIndexOf(' ');
                    instName = innerStr.substring(0, n3);
                    priceDay = LocalDate.parse(innerStr.substring(n3 + 1, n3 + 11), LazyHolder.DF);
                }

                isin = lines.get(++i);
                assertLine(i, isin, LazyHolder.ISIN_PATTERN);

            } catch (Exception e) {
                throw e;
            }

            Transaction t = new Transaction();
            t.setAccountNumber(accNumber);
            t.setType(tranType);
            t.setOrderDay(orderDay);
            t.setSettleDay(settleDay);
            t.setIsin(isin);
            t.setInstrumentName(instName);
            t.setDescription("%s %s".formatted(desc1, desc2).trim());
            t.setCurrency(Currency.EUR);
            t.setFees(fees.negate());
            t.setGrossAmount(grossAmount2.negate());
            t.setQuantity(qty);
            t.setPrice(unitPrice);
            t.setPriceDay(priceDay);
            trans.add(t);
        }
        return trans;
    }

    private void assertLine(int lineIdxZeroBased, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new InvalidStatementException("Unexpected %s.line: '%s'".formatted(lineIdxZeroBased + 1, actual));
        }
    }

    private void assertLine(int lineIdxZeroBased, String actual, Pattern expectedPattern) {
        if (actual == null || !expectedPattern.matcher(actual).matches()) {
            throw new InvalidStatementException("Unexpected %s.line: '%s'".formatted(lineIdxZeroBased + 1, actual));
        }
    }
}
