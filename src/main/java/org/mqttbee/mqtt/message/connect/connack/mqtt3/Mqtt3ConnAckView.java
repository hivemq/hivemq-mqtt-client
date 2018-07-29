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
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;
import org.mqttbee.util.MustNotBeImplementedUtil;

import javax.annotation.concurrent.Immutable;

import static org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode.*;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3ConnAckView implements Mqtt3ConnAck {

    @NotNull
    public static final Function<Mqtt5ConnAck, Mqtt3ConnAck> MAPPER = Mqtt3ConnAckView::of;

    @NotNull
    public static MqttConnAck delegate(
            @NotNull final Mqtt3ConnAckReturnCode returnCode, final boolean isSessionPresent) {

        return new MqttConnAck(delegateReasonCode(returnCode), isSessionPresent,
                MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, MqttConnAck.KEEP_ALIVE_FROM_CONNECT,
                MqttConnAck.CLIENT_IDENTIFIER_FROM_CONNECT, null, MqttConnAckRestrictions.DEFAULT, null, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    private static Mqtt5ConnAckReasonCode delegateReasonCode(@NotNull final Mqtt3ConnAckReturnCode returnCode) {
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

    @NotNull
    private static Mqtt3ConnAckReturnCode viewReasonCode(@NotNull final Mqtt5ConnAckReasonCode reasonCode) {
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

    @NotNull
    public static Mqtt3ConnAckView of(
            @NotNull final Mqtt3ConnAckReturnCode returnCode, final boolean isSessionPresent) {

        return new Mqtt3ConnAckView(delegate(returnCode, isSessionPresent));
    }

    @NotNull
    private static Mqtt3ConnAckView of(@NotNull final Mqtt5ConnAck connAck) {
        return new Mqtt3ConnAckView(MustNotBeImplementedUtil.checkNotImplemented(connAck, MqttConnAck.class));
    }

    @NotNull
    public static Mqtt3ConnAckView of(@NotNull final MqttConnAck connAck) {
        return new Mqtt3ConnAckView(connAck);
    }

    private final MqttConnAck delegate;

    private Mqtt3ConnAckView(@NotNull final MqttConnAck delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public Mqtt3ConnAckReturnCode getReturnCode() {
        return viewReasonCode(delegate.getReasonCode());
    }

    @Override
    public boolean isSessionPresent() {
        return delegate.isSessionPresent();
    }

    @NotNull
    public MqttConnAck getDelegate() {
        return delegate;
    }

}
