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

package org.mqttbee.mqtt.message.connect.connack.mqtt3;

import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;

import static org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode.*;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3ConnAckView implements Mqtt3ConnAck {

    public static final @NotNull Function<Mqtt5ConnAck, Mqtt3ConnAck> MAPPER = Mqtt3ConnAckView::of;

    public static @NotNull MqttConnAck delegate(
            final @NotNull Mqtt3ConnAckReturnCode returnCode, final boolean isSessionPresent) {

        return new MqttConnAck(delegateReasonCode(returnCode), isSessionPresent,
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
}
