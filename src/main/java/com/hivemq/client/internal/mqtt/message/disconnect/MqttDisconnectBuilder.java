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

package com.hivemq.client.internal.mqtt.message.disconnect;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttDisconnectBuilder<B extends MqttDisconnectBuilder<B>> {

    private @NotNull Mqtt5DisconnectReasonCode reasonCode = MqttDisconnect.DEFAULT_REASON_CODE;
    private @Range(from = -1, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval =
            MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private @Nullable MqttUtf8StringImpl serverReference;
    private @Nullable MqttUtf8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttDisconnectBuilder() {}

    MqttDisconnectBuilder(final @NotNull MqttDisconnect disconnect) {
        reasonCode = disconnect.getReasonCode();
        sessionExpiryInterval = disconnect.getRawSessionExpiryInterval();
        serverReference = disconnect.getRawServerReference();
        reasonString = disconnect.getRawReasonString();
        userProperties = disconnect.getUserProperties();
    }

    abstract @NotNull B self();

    public @NotNull B reasonCode(final @Nullable Mqtt5DisconnectReasonCode reasonCode) {
        this.reasonCode = Checks.notNull(reasonCode, "Reason Code");
        return self();
    }

    public @NotNull B sessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = Checks.unsignedInt(sessionExpiryInterval, "Session expiry interval");
        return self();
    }

    public @NotNull B noSessionExpiry() {
        this.sessionExpiryInterval = MqttConnect.NO_SESSION_EXPIRY;
        return self();
    }

    public @NotNull B serverReference(final @Nullable String serverReference) {
        this.serverReference = MqttChecks.stringOrNull(serverReference, "Server reference");
        return self();
    }

    public @NotNull B serverReference(final @Nullable MqttUtf8String serverReference) {
        this.serverReference = MqttChecks.stringOrNull(serverReference, "Server reference");
        return self();
    }

    public @NotNull B reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return self();
    }

    public @NotNull B reasonString(final @Nullable MqttUtf8String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return self();
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    public MqttUserPropertiesImplBuilder.@NotNull Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    public @NotNull MqttDisconnect build() {
        return new MqttDisconnect(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties);
    }

    public static class Default extends MqttDisconnectBuilder<Default> implements Mqtt5DisconnectBuilder {

        public Default() {}

        Default(final @NotNull MqttDisconnect disconnect) {
            super(disconnect);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttDisconnectBuilder<Nested<P>> implements Mqtt5DisconnectBuilder.Nested<P> {

        private final @NotNull Function<? super MqttDisconnect, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttDisconnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyDisconnect() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends MqttDisconnectBuilder<Send<P>> implements Mqtt5DisconnectBuilder.Send<P> {

        private final @NotNull Function<? super MqttDisconnect, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttDisconnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }

    public static class SendVoid extends MqttDisconnectBuilder<SendVoid> implements Mqtt5DisconnectBuilder.SendVoid {

        private final @NotNull Consumer<? super MqttDisconnect> parentConsumer;

        public SendVoid(final @NotNull Consumer<? super MqttDisconnect> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull SendVoid self() {
            return this;
        }

        @Override
        public void send() {
            parentConsumer.accept(build());
        }
    }
}
