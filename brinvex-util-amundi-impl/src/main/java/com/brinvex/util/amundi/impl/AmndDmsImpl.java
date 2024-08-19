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

import com.brinvex.util.amundi.api.service.AmndDms;
import com.brinvex.util.dms.api.Dms;

import static com.brinvex.util.amundi.impl.Util.getFirstThrowIfMore;

@SuppressWarnings("unused")
public class AmndDmsImpl implements AmndDms {

    private final Dms dms;

    public AmndDmsImpl(Dms dms) {
        this.dms = dms;
    }

    @Override
    public byte[] getStatementContent(String accountId) {
        String directory = getDirectory(accountId);
        String fileKey = getFirstThrowIfMore(dms.getKeys(directory));
        if (fileKey == null) {
            return null;
        }
        return dms.getBinaryContent(directory, fileKey);
    }

    private String getDirectory(String accountId) {
        return accountId;
    }
}
