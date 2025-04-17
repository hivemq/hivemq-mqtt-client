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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.mqtt.lifecycle.*;
import com.hivemq.client.mqtt.lifecycle.MqttClientReconnector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 * @author laokou
 */
public class MqttClientAutoReconnectImpl implements MqttClientAutoReconnect, MqttClientConnectedListener {

    static final long DEFAULT_START_DELAY_NANOS =
            TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_START_DELAY_S);
    static final long DEFAULT_MAX_DELAY_NANOS = TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_MAX_DELAY_S);
    static final int DEFAULT_MAX_RETRY_NUM = MqttClientAutoReconnect.DEFAULT_MAX_RETRY_NUM;
    public static final @NotNull MqttClientAutoReconnectImpl DEFAULT =
            new MqttClientAutoReconnectImpl(DEFAULT_START_DELAY_NANOS, DEFAULT_MAX_DELAY_NANOS, DEFAULT_MAX_RETRY_NUM);

    private final long initialDelayNanos;
    private final long maxDelayNanos;
    private final int maxRetryNum;
    private final AtomicInteger attempts = new AtomicInteger(0);

    MqttClientAutoReconnectImpl(final long initialDelayNanos, final long maxDelayNanos, final int maxRetryNum) {
        this.initialDelayNanos = initialDelayNanos;
        this.maxDelayNanos = maxDelayNanos;
        this.maxRetryNum = maxRetryNum;
    }

    @Override
    public void onDisconnected(final @NotNull MqttClientDisconnectedContext context) {
        final int num = attempts.incrementAndGet();
        if (context.getSource() != MqttDisconnectSource.USER && num <= maxRetryNum) {
            final MqttClientReconnector reconnector = context.getReconnector();
            final long delay =
                    (long) Math.min(initialDelayNanos * Math.pow(2, reconnector.getAttempts()), maxDelayNanos);
            final long randomDelay = (long) (delay / 4d / Integer.MAX_VALUE * ThreadLocalRandom.current().nextInt());
            reconnector.reconnect(true).delay(delay + randomDelay, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void onConnected(@NotNull final MqttClientConnectedContext context) {
        final int num = attempts.get();
        if (num > 0) {
            attempts.compareAndSet(num, 0);
        }
    }

    @Override
    public long getInitialDelay(final @NotNull TimeUnit timeUnit) {
        return timeUnit.convert(initialDelayNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getMaxDelay(final @NotNull TimeUnit timeUnit) {
        return timeUnit.convert(maxDelayNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public int getMaxRetryNum() {
        return maxRetryNum;
    }

    @Override
    public MqttClientAutoReconnectImplBuilder.@NotNull Default extend() {
        return new MqttClientAutoReconnectImplBuilder.Default(this);
    }

}
