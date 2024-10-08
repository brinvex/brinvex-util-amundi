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

import com.brinvex.util.amundi.api.model.statement.Trade;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface AmndParser {

    List<Trade> parseTransactionStatement(InputStream statementContent);

    default List<Trade> parseTransactionStatement(byte[] statementContent) {
        return parseTransactionStatement(new ByteArrayInputStream(statementContent));
    }

    static AmndParser create() {
        try {
            return (AmndParser) Class.forName("com.brinvex.util.amundi.impl.AmndParserImpl")
                    .getConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
