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

package org.mqttbee.mqtt.message.subscribe.suback.mqtt3;

import static org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode.FAILURE;
import static org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0;
import static org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1;
import static org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2;

import com.google.common.collect.ImmutableList;
import io.reactivex.functions.Function;
import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.util.MustNotBeImplementedUtil;

/** @author Silvio Giebl */
@Immutable
public class Mqtt3SubAckView implements Mqtt3SubAck {

    @NotNull public static final Function<Mqtt5SubAck, Mqtt3SubAck> MAPPER = Mqtt3SubAckView::of;

    @NotNull
    public static MqttSubAck delegate(
            final int packetIdentifier,
            @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        return new MqttSubAck(
                packetIdentifier,
                delegateReturnCodes(returnCodes),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    private static ImmutableList<Mqtt5SubAckReasonCode> delegateReturnCodes(
            @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        final ImmutableList.Builder<Mqtt5SubAckReasonCode> builder =
                ImmutableList.builderWithExpectedSize(returnCodes.size());
        for (int i = 0; i < returnCodes.size(); i++) {
            builder.add(delegateReturnCode(returnCodes.get(i)));
        }
        return builder.build();
    }

    @NotNull
    private static Mqtt5SubAckReasonCode delegateReturnCode(
            @NotNull final Mqtt3SubAckReturnCode returnCode) {
        switch (returnCode) {
            case SUCCESS_MAXIMUM_QOS_0:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_0;
            case SUCCESS_MAXIMUM_QOS_1:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_1;
            case SUCCESS_MAXIMUM_QOS_2:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_2;
            case FAILURE:
                return Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    private static ImmutableList<Mqtt3SubAckReturnCode> viewReasonCodes(
            @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes) {

        final ImmutableList.Builder<Mqtt3SubAckReturnCode> builder =
                ImmutableList.builderWithExpectedSize(reasonCodes.size());
        for (int i = 0; i < reasonCodes.size(); i++) {
            builder.add(viewReasonCode(reasonCodes.get(i)));
        }
        return builder.build();
    }

    @NotNull
    private static Mqtt3SubAckReturnCode viewReasonCode(
            @NotNull final Mqtt5SubAckReasonCode reasonCode) {
        switch (reasonCode) {
            case GRANTED_QOS_0:
                return SUCCESS_MAXIMUM_QOS_0;
            case GRANTED_QOS_1:
                return SUCCESS_MAXIMUM_QOS_1;
            case GRANTED_QOS_2:
                return SUCCESS_MAXIMUM_QOS_2;
            case UNSPECIFIED_ERROR:
                return FAILURE;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    public static Mqtt3SubAckView of(
            final int packetIdentifier,
            @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        return new Mqtt3SubAckView(delegate(packetIdentifier, returnCodes));
    }

    @NotNull
    private static Mqtt3SubAckView of(@NotNull final Mqtt5SubAck subAck) {
        return new Mqtt3SubAckView(
                MustNotBeImplementedUtil.checkNotImplemented(subAck, MqttSubAck.class));
    }

    @NotNull
    public static Mqtt3SubAckView of(@NotNull final MqttSubAck subAck) {
        return new Mqtt3SubAckView(subAck);
    }

    private final MqttSubAck delegate;

    private Mqtt3SubAckView(@NotNull final MqttSubAck delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt3SubAckReturnCode> getReturnCodes() {
        return viewReasonCodes(delegate.getReasonCodes());
    }

    @NotNull
    public MqttSubAck getDelegate() {
        return delegate;
    }
}
