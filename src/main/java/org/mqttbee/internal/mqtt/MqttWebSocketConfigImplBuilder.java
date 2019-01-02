/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.internal.mqtt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.MqttWebSocketConfigBuilder;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttWebSocketConfigImplBuilder<B extends MqttWebSocketConfigImplBuilder<B>> {

    private @NotNull String serverPath = MqttWebSocketConfigImpl.DEFAULT_SERVER_PATH;
    private @NotNull String subprotocol = MqttWebSocketConfigImpl.DEFAULT_MQTT_SUBPROTOCOL;

    abstract @NotNull B self();

    public @NotNull B serverPath(final @Nullable String serverPath) {
        // remove any leading slashes
        this.serverPath = Checks.notNull(serverPath, "Server path").replaceAll("^/+", "");
        return self();
    }

    public @NotNull B subprotocol(final @Nullable String subprotocol) {
        this.subprotocol = Checks.notNull(subprotocol, "Subprotocol");
        return self();
    }

    public @NotNull MqttWebSocketConfigImpl build() {
        return new MqttWebSocketConfigImpl(serverPath, subprotocol);
    }

    public static class Default extends MqttWebSocketConfigImplBuilder<Default> implements MqttWebSocketConfigBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttWebSocketConfigImplBuilder<Nested<P>>
            implements MqttWebSocketConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttWebSocketConfigImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttWebSocketConfigImpl, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyWebSicketConfig() {
            return parentConsumer.apply(build());
        }
    }
}
