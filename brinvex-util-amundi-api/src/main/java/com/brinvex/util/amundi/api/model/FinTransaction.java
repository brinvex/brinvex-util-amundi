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
package com.brinvex.util.amundi.api.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinTransaction(
        String id,
        LocalDate date,
        TransactionType type,
        String isin,
        BigDecimal qty,
        Currency ccy,
        BigDecimal price,
        BigDecimal grossValue,
        BigDecimal netValue,
        BigDecimal fee,
        LocalDate settleDay
) {
}