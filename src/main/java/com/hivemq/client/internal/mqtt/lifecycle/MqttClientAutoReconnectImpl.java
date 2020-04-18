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

import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientReconnector;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class MqttClientAutoReconnectImpl implements MqttClientAutoReconnect {

    static final long DEFAULT_START_DELAY_NANOS =
            TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_START_DELAY_S);
    static final long DEFAULT_MAX_DELAY_NANOS = TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_MAX_DELAY_S);
    public static final @NotNull MqttClientAutoReconnectImpl DEFAULT =
            new MqttClientAutoReconnectImpl(DEFAULT_START_DELAY_NANOS, DEFAULT_MAX_DELAY_NANOS);

    private final long initialDelayNanos;
    private final long maxDelayNanos;

    MqttClientAutoReconnectImpl(final long initialDelayNanos, final long maxDelayNanos) {
        this.initialDelayNanos = initialDelayNanos;
        this.maxDelayNanos = maxDelayNanos;
    }

    @Override
    public void onDisconnected(final @NotNull MqttClientDisconnectedContext context) {
        if (context.getSource() != MqttDisconnectSource.USER) {
            final MqttClientReconnector reconnector = context.getReconnector();
            final long delay =
                    (long) Math.min(initialDelayNanos * Math.pow(2, reconnector.getAttempts()), maxDelayNanos);
            final long randomDelay = (long) (delay / 4d / Integer.MAX_VALUE * ThreadLocalRandom.current().nextInt());
            reconnector.reconnect(true).delay(delay + randomDelay, TimeUnit.NANOSECONDS);
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
    public @NotNull MqttClientAutoReconnectImplBuilder.Default extend() {
        return new MqttClientAutoReconnectImplBuilder.Default(this);
    }
}
