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

import java.math.BigDecimal;
import java.util.Collection;

class Util {

    static BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        s = deleteAllWhitespaces(s);
        s = s.replace(',', '.');
        return new BigDecimal(s);
    }

    static String deleteAllWhitespaces(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        final int sz = str.length();
        final char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        if (count == 0) {
            return "";
        }
        return new String(chs, 0, count);
    }

    public static <E> E getFirstThrowIfMore(Collection<E> collection) {
        int size = collection.size();
        return switch (size) {
            case 0 -> null;
            case 1 -> collection.iterator().next();
            default -> throw new IllegalStateException(
                    "Expecting empty or one-element collection but got #%s, %s".formatted(size, collection));
        };
    }


}
