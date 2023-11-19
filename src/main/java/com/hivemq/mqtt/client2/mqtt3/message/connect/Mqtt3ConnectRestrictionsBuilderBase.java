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

package com.hivemq.mqtt.client2.mqtt3.message.connect;

import com.hivemq.mqtt.client2.internal.datatypes.MqttVariableByteInteger;
import com.hivemq.mqtt.client2.internal.util.UnsignedDataTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Builder base for {@link Mqtt3ConnectRestrictions}.
 *
 * @param <B> the type of the builder.
 * @author Yannick Weber
 * @since 1.3
 */
@ApiStatus.NonExtendable
public interface Mqtt3ConnectRestrictionsBuilderBase<B extends Mqtt3ConnectRestrictionsBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt3ConnectRestrictions#getSendMaximum() send maximum}.
     * <p>
     * The value must not be zero and must be in the range of an unsigned short: [1, 65_535].
     *
     * @param receiveMaximum the send maximum.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sendMaximum(@Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum);

    /**
     * Sets the {@link Mqtt3ConnectRestrictions#getSendMaximumPacketSize() maximum packet size for sending}.
     * <p>
     * The value must not be zero and in the range: [1, 268_435_460].
     *
     * @param maximumPacketSize the maximum packet size for sending.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sendMaximumPacketSize(
            @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize);
}
