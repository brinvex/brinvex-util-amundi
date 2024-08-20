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

import com.brinvex.util.amundi.api.model.FinTransaction;
import com.brinvex.util.amundi.api.model.FinTransactionType;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Accessors(fluent = true, chain = true)
public class FinTransactionBuilder {
    private String id;
    private FinTransactionType type;
    private LocalDate date;
    private String isin;
    private BigDecimal qty;
    private String ccy;
    private BigDecimal price;
    private BigDecimal grossValue;
    private BigDecimal netValue;
    private BigDecimal fee;
    private LocalDate settleDay;

    public FinTransaction build() {
        return new FinTransaction(
                id,
                type,
                date,
                isin,
                qty,
                ccy,
                price,
                grossValue,
                netValue,
                fee,
                settleDay
        );
    }

}
