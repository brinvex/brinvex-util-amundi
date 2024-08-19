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
import com.brinvex.util.amundi.api.model.FinTransaction;
import com.brinvex.util.amundi.api.model.TransactionType;
import com.brinvex.util.amundi.api.model.statement.Trade;
import com.brinvex.util.amundi.api.service.AmndDms;
import com.brinvex.util.amundi.api.service.AmndParser;
import com.brinvex.util.amundi.api.service.AmndPtfTracker;
import com.brinvex.util.amundi.api.service.exception.AmndException;
import com.brinvex.util.amundi.impl.builder.FinTransactionBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public class AmndPtfTrackerImpl implements AmndPtfTracker {

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
        String id = trade.id();
        LocalDate orderDay = trade.orderDate();
        String isin = trade.isin();
        BigDecimal qty = trade.quantity();
        BigDecimal price = trade.price();
        BigDecimal fees = trade.fee();
        BigDecimal tax = BigDecimal.ZERO;
        Currency ccy = trade.currency();
        BigDecimal netValue = trade.netAmount();
        BigDecimal grossValue = netValue.subtract(fees);
        LocalDate settleDate = trade.settleDate();

        return switch (trade.type()) {
            case BUY -> List.of(
                    new FinTransactionBuilder()
                            .type(TransactionType.DEPOSIT)
                            .id("DEPOSIT/" + id)
                            .date(orderDay)
                            .qty(BigDecimal.ZERO)
                            .ccy(ccy)
                            .grossValue(netValue.negate())
                            .netValue(netValue.negate())
                            .fee(BigDecimal.ZERO)
                            .settleDay(settleDate)
                            .build(),
                    new FinTransactionBuilder()
                            .type(TransactionType.BUY)
                            .id(id)
                            .date(orderDay)
                            .isin(isin)
                            .qty(qty)
                            .price(price)
                            .ccy(ccy)
                            .grossValue(grossValue)
                            .netValue(netValue)
                            .fee(fees)
                            .settleDay(settleDate)
                            .build()
            );
            case SELL -> List.of(
                    new FinTransactionBuilder()
                            .type(TransactionType.SELL)
                            .id(id)
                            .date(orderDay)
                            .isin(isin)
                            .qty(qty)
                            .price(price)
                            .ccy(ccy)
                            .grossValue(grossValue)
                            .netValue(netValue)
                            .fee(fees)
                            .settleDay(settleDate)
                            .build(),
                    new FinTransactionBuilder()
                            .type(TransactionType.WITHDRAWAL)
                            .id("WITHDRAWAL/" + id)
                            .date(orderDay)
                            .qty(BigDecimal.ZERO)
                            .ccy(ccy)
                            .grossValue(netValue.negate())
                            .netValue(netValue.negate())
                            .fee(BigDecimal.ZERO)
                            .settleDay(settleDate)
                            .build()
            );
        };
    }
}
