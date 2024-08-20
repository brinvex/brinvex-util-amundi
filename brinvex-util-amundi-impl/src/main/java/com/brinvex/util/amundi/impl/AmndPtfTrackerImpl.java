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

import com.brinvex.util.amundi.api.model.FinTransaction;
import com.brinvex.util.amundi.api.model.FinTransactionType;
import com.brinvex.util.amundi.api.model.statement.Trade;
import com.brinvex.util.amundi.api.service.AmndDms;
import com.brinvex.util.amundi.api.service.AmndParser;
import com.brinvex.util.amundi.api.service.AmndPtfTracker;
import com.brinvex.util.amundi.api.service.exception.AmndException;
import com.brinvex.util.amundi.impl.builder.FinTransactionBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public class AmndPtfTrackerImpl implements AmndPtfTracker {

    private static class Lazy {
        private static final DateTimeFormatter ID_DF8 = DateTimeFormatter.ofPattern("yyyyMMdd");
        private static final DateTimeFormatter ID_DF6 = DateTimeFormatter.ofPattern("yyMMdd");
    }

    private final AmndDms amndDms;

    private final AmndParser amndParser;

    public AmndPtfTrackerImpl(AmndDms amndDms, AmndParser amndParser) {
        this.amndDms = amndDms;
        this.amndParser = amndParser;
    }

    @Override
    public List<FinTransaction> getFinTransactions(String accountId, LocalDate fromDayIncl, LocalDate toDayIncl) {
        byte[] statementContent = amndDms.getStatementContent(accountId);
        if (statementContent == null) {
            throw new AmndException("No statement found in DMS for accountId=%s".formatted(accountId));
        }

        return amndParser.parseTransactionStatement(new ByteArrayInputStream(statementContent))
                .stream()
                .filter(t -> (fromDayIncl == null || !t.orderDate().isBefore(fromDayIncl)) && (toDayIncl == null || !t.orderDate().isAfter(toDayIncl)))
                .sorted(comparing(Trade::orderDate).thenComparing(Trade::tradeDate).thenComparing(Trade::settleDate))
                .peek(t -> {
                    if (!t.accountId().equals(accountId)) {
                        throw new AmndException("Statement accountId=%s does not match given accountId=%s".formatted(t.accountId(), accountId));
                    }
                })
                .map(this::mapTradeToFinTransactionPair)
                .flatMap(Collection::stream)
                .collect(toMap(FinTransaction::id, identity(), (u, v) -> {
                    throw new AmndException("FinTransaction ID conflict: %s, %s".formatted(u, v));
                }, LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    private List<FinTransaction> mapTradeToFinTransactionPair(Trade trade) {

        BigDecimal qty = trade.qty();
        BigDecimal fees = trade.fee();
        BigDecimal netValue = trade.netValue();
        BigDecimal grossValue = netValue.subtract(fees);

        String id = "%s/%s/%s/%s/%s".formatted(
                Lazy.ID_DF8.format(trade.orderDate()),
                Lazy.ID_DF6.format(trade.tradeDate()),
                Lazy.ID_DF6.format(trade.settleDate()),
                netValue.setScale(2, RoundingMode.HALF_UP),
                qty.setScale(2, RoundingMode.HALF_UP));

        return switch (trade.type()) {
            case BUY -> List.of(
                    new FinTransactionBuilder()
                            .type(FinTransactionType.DEPOSIT)
                            .id(id + "/1/DEPOSIT")
                            .date(trade.orderDate())
                            .qty(BigDecimal.ZERO)
                            .ccy(trade.ccy())
                            .grossValue(netValue.negate())
                            .netValue(netValue.negate())
                            .fee(BigDecimal.ZERO)
                            .settleDay(trade.settleDate())
                            .build(),
                    new FinTransactionBuilder()
                            .type(FinTransactionType.BUY)
                            .id(id + "/2/BUY")
                            .date(trade.orderDate())
                            .isin(trade.isin())
                            .qty(qty)
                            .price(trade.price())
                            .ccy(trade.ccy())
                            .grossValue(grossValue)
                            .netValue(netValue)
                            .fee(fees)
                            .settleDay(trade.settleDate())
                            .build()
            );
            case SELL -> List.of(
                    new FinTransactionBuilder()
                            .type(FinTransactionType.SELL)
                            .id(id + "/3/SELL")
                            .date(trade.orderDate())
                            .isin(trade.isin())
                            .qty(qty)
                            .price(trade.price())
                            .ccy(trade.ccy())
                            .grossValue(grossValue)
                            .netValue(netValue)
                            .fee(fees)
                            .settleDay(trade.settleDate())
                            .build(),
                    new FinTransactionBuilder()
                            .type(FinTransactionType.WITHDRAWAL)
                            .id(id + "/4/WITHDRAWAL")
                            .date(trade.orderDate())
                            .qty(BigDecimal.ZERO)
                            .ccy(trade.ccy())
                            .grossValue(netValue.negate())
                            .netValue(netValue.negate())
                            .fee(BigDecimal.ZERO)
                            .settleDay(trade.settleDate())
                            .build()
            );
        };
    }
}
