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

public interface AmndDms {

    byte[] getStatementContent(String accountId);

    static AmndDms create(com.brinvex.util.dms.api.Dms dms) {
        try {
            return (AmndDms) Class.forName("com.brinvex.util.amundi.impl.AmndDmsImpl")
                    .getConstructor(com.brinvex.util.dms.api.Dms.class)
                    .newInstance(dms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
