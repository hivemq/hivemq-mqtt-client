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

import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.util.collections.ImmutableList;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SubAckView implements Mqtt3SubAck {

    public static final @NotNull Function<Mqtt5SubAck, Mqtt3SubAck> MAPPER = Mqtt3SubAckView::of;

    public static @NotNull MqttSubAck delegate(
            final int packetIdentifier, final @NotNull ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        return new MqttSubAck(
                packetIdentifier, delegateReturnCodes(returnCodes), null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private static @NotNull ImmutableList<Mqtt5SubAckReasonCode> delegateReturnCodes(
            final @NotNull ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        final ImmutableList.Builder<Mqtt5SubAckReasonCode> builder = ImmutableList.builder(returnCodes.size());
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < returnCodes.size(); i++) {
            builder.add(delegateReturnCode(returnCodes.get(i)));
        }
        return builder.build();
    }

    private static @NotNull Mqtt5SubAckReasonCode delegateReturnCode(final @NotNull Mqtt3SubAckReturnCode returnCode) {
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

    private static @NotNull ImmutableList<Mqtt3SubAckReturnCode> viewReasonCodes(
            final @NotNull ImmutableList<Mqtt5SubAckReasonCode> reasonCodes) {

        final ImmutableList.Builder<Mqtt3SubAckReturnCode> builder = ImmutableList.builder(reasonCodes.size());
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < reasonCodes.size(); i++) {
            builder.add(viewReasonCode(reasonCodes.get(i)));
        }
        return builder.build();
    }

    private static @NotNull Mqtt3SubAckReturnCode viewReasonCode(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        switch (reasonCode) {
            case GRANTED_QOS_0:
                return Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0;
            case GRANTED_QOS_1:
                return Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1;
            case GRANTED_QOS_2:
                return Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2;
            case UNSPECIFIED_ERROR:
                return Mqtt3SubAckReturnCode.FAILURE;
            default:
                throw new IllegalStateException();
        }
    }

    public static @NotNull Mqtt3SubAckView of(final @NotNull Mqtt5SubAck subAck) {
        return new Mqtt3SubAckView((MqttSubAck) subAck);
    }

    public static @NotNull Mqtt3SubAckView of(final @NotNull MqttSubAck subAck) {
        return new Mqtt3SubAckView(subAck);
    }

    private final @NotNull MqttSubAck delegate;

    private Mqtt3SubAckView(final @NotNull MqttSubAck delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull ImmutableList<@NotNull Mqtt3SubAckReturnCode> getReturnCodes() {
        return viewReasonCodes(delegate.getReasonCodes());
    }

    public @NotNull MqttSubAck getDelegate() {
        return delegate;
    }
}
