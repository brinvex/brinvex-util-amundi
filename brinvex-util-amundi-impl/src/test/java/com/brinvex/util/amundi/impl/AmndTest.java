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
import com.brinvex.util.dms.api.Dms;
import com.brinvex.util.dms.api.DmsFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static java.time.LocalDate.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpellCheckingInspection")
class AmndTest {

    private static final String TEST_DATA_FOLDER = "c:/prj/bx-util/bx-util-amundi/test-data";

    private static final DmsFactory dmsFactory = DmsFactory.createFilesystemDmsFactory(Path.of(TEST_DATA_FOLDER));

    private static final Dms dms = dmsFactory.getDms("dms");

    private static final AmndDms amndDms = AmndDms.create(dms);

    private static final AmndParser amndParser = AmndParser.create();

    private static final AmndPtfTracker amndPtfTracker = AmndPtfTracker.create(amndDms, amndParser);

    private static String accounId1;
    private static String accounId2;
    private static String accounId3;

    static {
        Path accountsPath = Path.of(TEST_DATA_FOLDER, "accounts.txt");
        if (Files.exists(accountsPath)) {
            try {
                List<String> lines = Files.readAllLines(accountsPath);
                int size = lines.size();
                accounId1 = size > 0 ? lines.get(0) : null;
                accounId2 = size > 1 ? lines.get(1) : null;
                accounId3 = size > 2 ? lines.get(2) : null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static boolean testAccount1IsNotNull() {
        return accounId1 != null;
    }

    private static boolean testAccount2IsNotNull() {
        return accounId2 != null;
    }

    private static boolean testAccount3IsNotNull() {
        return accounId3 != null;
    }


    @EnabledIf("testAccount1IsNotNull")
    @Test
    void parseTransactionStatements1() {
        List<Trade> trades = amndParser.parseTransactionStatement(amndDms.getStatementContent(accounId1))
                .stream()
                .filter(t -> !t.orderDate().isAfter(parse("2023-08-06")))
                .toList();
        assertNotNull(trades);
        assertEquals(122, trades.size());
        assertEquals(new ArrayList<>(new LinkedHashSet<>(trades)), trades);
    }

    @EnabledIf("testAccount2IsNotNull")
    @Test
    void parseTransactionStatements2() {
        List<Trade> trades = amndParser.parseTransactionStatement(amndDms.getStatementContent(accounId2))
                .stream()
                .filter(t -> !t.orderDate().isAfter(parse("2023-08-06")))
                .toList();
        assertNotNull(trades);
        assertEquals(106, trades.size());
        assertEquals(new ArrayList<>(new LinkedHashSet<>(trades)), trades);
    }

    @EnabledIf("testAccount3IsNotNull")
    @Test
    void parseTransactionStatements3() {
        List<Trade> trades = amndParser.parseTransactionStatement(amndDms.getStatementContent(accounId3))
                .stream()
                .filter(t -> !t.orderDate().isAfter(parse("2023-08-06")))
                .toList();
        assertNotNull(trades);
        assertEquals(119, trades.size());
        assertEquals(new ArrayList<>(new LinkedHashSet<>(trades)), trades);
    }

    @EnabledIf("testAccount1IsNotNull")
    @Test
    void progress1() {
        List<FinTransaction> finTransactions;
        {
            finTransactions = amndPtfTracker.getFinTransactions(accounId1, null, parse("2023-08-06"));
            assertEquals(122 * 2, finTransactions.size());
        }
        {
            finTransactions = amndPtfTracker.getFinTransactions(accounId1, null, parse("2024-08-19"));
            assertEquals(130 * 2, finTransactions.size());

            {
                FinTransaction tran = finTransactions.getFirst();
                assertEquals(FinTransactionType.DEPOSIT, tran.type());
                assertNull(tran.isin());
                assertEquals(new BigDecimal("0"), tran.qty());
                assertEquals("EUR", tran.ccy());
                assertNull(tran.price());
                assertEquals(new BigDecimal("400.00"), tran.grossValue());
                assertEquals(new BigDecimal("400.00"), tran.netValue());
                assertEquals(new BigDecimal("0"), tran.fee());
                assertEquals(parse("2017-02-03"), tran.settleDay());
            }
            {
                FinTransaction tran = finTransactions.get(1);
                assertEquals(FinTransactionType.BUY, tran.type());
                assertEquals("LU1121647660", tran.isin());
                assertEquals(new BigDecimal("16.435"), tran.qty());
                assertEquals("EUR", tran.ccy());
                assertEquals(new BigDecimal("7.910000"), tran.price());
                assertEquals(new BigDecimal("-130.00"), tran.grossValue());
                assertEquals(new BigDecimal("-400.00"), tran.netValue());
                assertEquals(new BigDecimal("-270.00"), tran.fee());
                assertEquals(parse("2017-02-03"), tran.settleDay());
            }

            {
                FinTransaction tran = finTransactions.getLast();
                assertEquals(FinTransactionType.WITHDRAWAL, tran.type());
                assertNull(tran.isin());
                assertEquals(new BigDecimal("0"), tran.qty());
                assertEquals("EUR", tran.ccy());
                assertNull(tran.price());
                assertEquals(new BigDecimal("-406.50"), tran.grossValue());
                assertEquals(new BigDecimal("-406.50"), tran.netValue());
                assertEquals(new BigDecimal("0"), tran.fee());
                assertEquals(parse("2024-08-09"), tran.settleDay());
            }
            {
                FinTransaction tran = finTransactions.get(finTransactions.size() - 2);
                assertEquals(FinTransactionType.SELL, tran.type());
                assertEquals("LU1121647660", tran.isin());
                assertEquals(new BigDecimal("-50.000"), tran.qty());
                assertEquals("EUR", tran.ccy());
                assertEquals(new BigDecimal("8.130000"), tran.price());
                assertEquals(new BigDecimal("406.50"), tran.grossValue());
                assertEquals(new BigDecimal("406.50"), tran.netValue());
                assertEquals(new BigDecimal("0.00"), tran.fee());
                assertEquals(parse("2024-08-09"), tran.settleDay());
            }

        }
    }

    @EnabledIf("testAccount2IsNotNull")
    @Test
    void progress2() {
        List<FinTransaction> finTransactions;
        {
            finTransactions = amndPtfTracker.getFinTransactions(accounId2, null, parse("2023-08-06"));
            assertEquals(106 * 2, finTransactions.size());
        }
    }

    @EnabledIf("testAccount3IsNotNull")
    @Test
    void progress3() {
        List<FinTransaction> finTransactions;
        {
            finTransactions = amndPtfTracker.getFinTransactions(accounId3, null, parse("2023-08-06"));
            assertEquals(119 * 2, finTransactions.size());
        }
    }

}
