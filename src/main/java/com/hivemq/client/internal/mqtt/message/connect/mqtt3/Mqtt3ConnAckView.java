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

package com.hivemq.client.internal.mqtt.message.connect.mqtt3;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAckRestrictions;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckReasonCode;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAckReturnCode.*;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3ConnAckView implements Mqtt3ConnAck {

    public static final @NotNull Function<Mqtt5ConnAck, Mqtt3ConnAck> MAPPER = Mqtt3ConnAckView::of;

    public static @NotNull MqttConnAck delegate(
            final @NotNull Mqtt3ConnAckReturnCode returnCode, final boolean sessionPresent) {

        return new MqttConnAck(delegateReasonCode(returnCode), sessionPresent,
                MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, MqttConnAck.KEEP_ALIVE_FROM_CONNECT, null, null,
                MqttConnAckRestrictions.DEFAULT, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private static @NotNull Mqtt5ConnAckReasonCode delegateReasonCode(
            final @NotNull Mqtt3ConnAckReturnCode returnCode) {

        switch (returnCode) {
            case SUCCESS:
                return Mqtt5ConnAckReasonCode.SUCCESS;
            case UNSUPPORTED_PROTOCOL_VERSION:
                return Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION;
            case IDENTIFIER_REJECTED:
                return Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;
            case SERVER_UNAVAILABLE:
                return Mqtt5ConnAckReasonCode.SERVER_UNAVAILABLE;
            case BAD_USER_NAME_OR_PASSWORD:
                return Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
            case NOT_AUTHORIZED:
                return Mqtt5ConnAckReasonCode.NOT_AUTHORIZED;
            default:
                throw new IllegalStateException();
        }
    }

    private static @NotNull Mqtt3ConnAckReturnCode viewReasonCode(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        switch (reasonCode) {
            case SUCCESS:
                return SUCCESS;
            case UNSUPPORTED_PROTOCOL_VERSION:
                return UNSUPPORTED_PROTOCOL_VERSION;
            case CLIENT_IDENTIFIER_NOT_VALID:
                return IDENTIFIER_REJECTED;
            case SERVER_UNAVAILABLE:
                return SERVER_UNAVAILABLE;
            case BAD_USER_NAME_OR_PASSWORD:
                return BAD_USER_NAME_OR_PASSWORD;
            case NOT_AUTHORIZED:
                return NOT_AUTHORIZED;
            default:
                throw new IllegalStateException();
        }
    }

    public static @NotNull Mqtt3ConnAckView of(final @NotNull Mqtt5ConnAck connAck) {
        return new Mqtt3ConnAckView((MqttConnAck) connAck);
    }

    public static @NotNull Mqtt3ConnAckView of(final @NotNull MqttConnAck connAck) {
        return new Mqtt3ConnAckView(connAck);
    }

    private final @NotNull MqttConnAck delegate;

    private Mqtt3ConnAckView(final @NotNull MqttConnAck delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3ConnAckReturnCode getReturnCode() {
        return viewReasonCode(delegate.getReasonCode());
    }

    @Override
    public boolean isSessionPresent() {
        return delegate.isSessionPresent();
    }

    public @NotNull MqttConnAck getDelegate() {
        return delegate;
    }

    private @NotNull String toAttributeString() {
        return "returnCode=" + getReturnCode() + ", sessionPresent=" + isSessionPresent();
    }

    @Override
    public @NotNull String toString() {
        return "MqttConnAck{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3ConnAckView)) {
            return false;
        }
        final Mqtt3ConnAckView that = (Mqtt3ConnAckView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
