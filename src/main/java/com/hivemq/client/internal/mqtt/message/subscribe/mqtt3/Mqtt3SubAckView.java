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

package com.hivemq.client.internal.mqtt.message.subscribe.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubAck;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAckReasonCode;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
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
    public @NotNull ImmutableList<Mqtt3SubAckReturnCode> getReturnCodes() {
        return viewReasonCodes(delegate.getReasonCodes());
    }

    public @NotNull MqttSubAck getDelegate() {
        return delegate;
    }

    private @NotNull String toAttributeString() {
        return "returnCodes=" + getReturnCodes();
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubAck{" + toAttributeString() + "}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3SubAckView)) {
            return false;
        }
        final Mqtt3SubAckView that = (Mqtt3SubAckView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
