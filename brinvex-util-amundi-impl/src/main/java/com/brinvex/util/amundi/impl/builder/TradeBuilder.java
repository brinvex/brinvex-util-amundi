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
package com.brinvex.util.amundi.impl.builder;

import com.brinvex.util.amundi.api.model.Currency;
import com.brinvex.util.amundi.api.model.statement.Trade;
import com.brinvex.util.amundi.api.model.statement.TradeType;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Accessors(fluent = true, chain = true)
public class TradeBuilder {
    private String id;
    private String accountId;
    private TradeType type;
    private LocalDate orderDate;
    private LocalDate tradeDate;
    private BigDecimal netAmount;
    private BigDecimal fees;
    private BigDecimal quantity;
    private BigDecimal price;
    private LocalDate priceDate;
    private Currency currency;
    private String isin;
    private String instrumentName;
    private String description;
    private LocalDate settleDate;

    public Trade build() {
        return new Trade(
                id,
                accountId,
                type,
                orderDate,
                tradeDate,
                netAmount,
                fees,
                quantity,
                price,
                priceDate,
                currency,
                isin,
                instrumentName,
                description,
                settleDate
        );
    }

}
