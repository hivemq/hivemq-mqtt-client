/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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

package com.hivemq.client2.internal.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 */
class ExecutorUtilTest {

    @Test
    void execute() throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            assertTrue(ExecutorUtil.execute(executorService, latch::countDown));
            assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        } finally {
            executorService.shutdown();
        }
    }

    @Test
    void execute_isShutdown() throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.shutdown();
        final CountDownLatch latch = new CountDownLatch(1);
        assertFalse(ExecutorUtil.execute(executorService, latch::countDown));
        assertFalse(latch.await(10, TimeUnit.MILLISECONDS));
    }
}