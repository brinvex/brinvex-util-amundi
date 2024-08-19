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
package com.brinvex.util.amundi.api.service;

import com.brinvex.util.amundi.api.model.FinTransaction;

import java.time.LocalDate;
import java.util.List;

public interface AmndPtfTracker {

    List<FinTransaction> getFinTransactions(
            String accountId,
            LocalDate fromDayIncl,
            LocalDate toDayIncl);

    static AmndPtfTracker create(com.brinvex.util.dms.api.Dms dms) {
        return create(
                AmndDms.create(dms),
                AmndParser.create()
        );
    }

    static AmndPtfTracker create(AmndDms dms, AmndParser parser) {
        try {
            return (AmndPtfTracker) Class.forName("com.brinvex.util.amundi.impl.AmndPtfTrackerImpl")
                    .getConstructor(AmndDms.class, AmndParser.class)
                    .newInstance(dms, parser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
