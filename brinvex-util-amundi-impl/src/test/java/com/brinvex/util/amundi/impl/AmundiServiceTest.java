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

import com.brinvex.util.amundi.api.model.Transaction;
import com.brinvex.util.amundi.api.service.AmundiService;
import com.brinvex.util.amundi.api.service.AmundiServiceFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AmundiServiceTest {

    private static final String TEST_DATA_FOLDER = "c:/prj/brinvex/brinvex-util/brinvex-util-amundi/test-data";

    private final AmundiService amundiSvc = AmundiServiceFactory.INSTANCE.getService();

    @Test
    void parseTransactionStatements1() throws IOException {
        List<Path> testFilePaths = getTestFilePaths(f -> f.equals("Transakcny vypis - Conservative-20170130-20230806.pdf"));
        if (!testFilePaths.isEmpty()) {
            for (Path testFilePath : testFilePaths) {
                try (InputStream is = new FileInputStream(testFilePath.toFile())) {
                    List<Transaction> trans = amundiSvc.parseTransactionStatements(is);
                    assertNotNull(trans);
                    assertEquals(122, trans.size());
                }
            }
        }
    }

    @Test
    void parseTransactionStatements2() throws IOException {
        List<Path> testFilePaths = getTestFilePaths(f -> f.equals("Transakcny vypis - Balanced-20170130-20230806.pdf"));
        if (!testFilePaths.isEmpty()) {
            for (Path testFilePath : testFilePaths) {
                try (InputStream is = new FileInputStream(testFilePath.toFile())) {
                    List<Transaction> trans = amundiSvc.parseTransactionStatements(is);
                    assertNotNull(trans);
                }
            }
        }
    }

    @Test
    void parseTransactionStatements3() throws IOException {
        List<Path> testFilePaths = getTestFilePaths(f -> f.equals("Transakcny vypis - Dynamic-20170130-20230806.pdf"));
        if (!testFilePaths.isEmpty()) {
            for (Path testFilePath : testFilePaths) {
                try (InputStream is = new FileInputStream(testFilePath.toFile())) {
                    List<Transaction> trans = amundiSvc.parseTransactionStatements(is);
                    assertNotNull(trans);
                }
            }
        }
    }

    private List<Path> getTestFilePaths() {
        return getTestFilePaths(fileName -> true);
    }

    private List<Path> getTestFilePaths(Predicate<String> fileNameFilter) {
        String testDataFolder = TEST_DATA_FOLDER;

        List<Path> testStatementFilePaths;
        Path testFolderPath = Paths.get(testDataFolder);
        File testFolder = testFolderPath.toFile();
        if (!testFolder.exists() || !testFolder.isDirectory()) {
            out.printf(String.format("Test data folder not found: '%s'", testDataFolder));
        }
        try (Stream<Path> filePaths = Files.walk(testFolderPath)) {
            testStatementFilePaths = filePaths
                    .filter(p -> fileNameFilter.test(p.getFileName().toString()))
                    .filter(p -> p.toFile().isFile())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (testStatementFilePaths.isEmpty()) {
            out.printf(String.format("No files found in test data folder: '%s'", testDataFolder));
        }
        return testStatementFilePaths;
    }
}
