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

package com.hivemq.client.internal.mqtt.advanced.mqtt3;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.advanced.MqttClientAdvancedConfig;
import com.hivemq.client.internal.mqtt.advanced.MqttClientAdvancedConfigBuilder;
import com.hivemq.client.mqtt.mqtt3.advanced.Mqtt3ClientAdvancedConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Abdullah Imal
 */
@Immutable
public class Mqtt3ClientAdvancedConfigView implements Mqtt3ClientAdvancedConfig {

    public static final @NotNull Mqtt3ClientAdvancedConfigView DEFAULT = of(MqttClientAdvancedConfig.DEFAULT);

    private static @NotNull MqttClientAdvancedConfig delegate(final int maxConcurrentPublishes) {
        return new MqttClientAdvancedConfigBuilder.Default().maxConcurrentPublishes(maxConcurrentPublishes).build();
    }

    static @NotNull Mqtt3ClientAdvancedConfigView of(final int maxConcurrentPublishes) {
        return new Mqtt3ClientAdvancedConfigView(delegate(maxConcurrentPublishes));
    }

    public static @NotNull Mqtt3ClientAdvancedConfigView of(final @NotNull MqttClientAdvancedConfig delegate) {
        return new Mqtt3ClientAdvancedConfigView(delegate);
    }

    private final @NotNull MqttClientAdvancedConfig delegate;

    private Mqtt3ClientAdvancedConfigView(final @NotNull MqttClientAdvancedConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public int maxConcurrentPublishes() {
        return delegate.maxConcurrentPublishes();
    }

    public @NotNull MqttClientAdvancedConfig getDelegate() {
        return delegate;
    }

    @Override
    public Mqtt3ClientAdvancedConfigViewBuilder.@NotNull Default extend() {
        return new Mqtt3ClientAdvancedConfigViewBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Mqtt3ClientAdvancedConfigView that = (Mqtt3ClientAdvancedConfigView) o;
        return Objects.equals(getDelegate(), that.getDelegate());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDelegate());
    }
}
