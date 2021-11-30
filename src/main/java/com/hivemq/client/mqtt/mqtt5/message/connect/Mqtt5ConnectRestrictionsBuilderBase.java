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

package com.hivemq.client.mqtt.mqtt5.message.connect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder base for {@link Mqtt5ConnectRestrictions}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ConnectRestrictionsBuilderBase<B extends Mqtt5ConnectRestrictionsBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getReceiveMaximum() receive maximum}.
     * <p>
     * The value must not be zero and must be in the range of an unsigned short: [1, 65_535].
     *
     * @param receiveMaximum the receive maximum.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B receiveMaximum(int receiveMaximum);

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getSendMaximum() send maximum}.
     * <p>
     * The value must not be zero and must be in the range of an unsigned short: [1, 65_535].
     * <p>
     * WARNING: Do not confuse this with {@link #receiveMaximum(int)}.
     *
     * @param receiveMaximum the send maximum.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sendMaximum(int receiveMaximum);

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getMaximumPacketSize() maximum packet size}.
     * <p>
     * The value must not be zero and in the range: [1, 268_435_460].
     *
     * @param maximumPacketSize the maximum packet size.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B maximumPacketSize(int maximumPacketSize);

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getSendMaximumPacketSize() maximum packet size for sending}.
     * <p>
     * The value must not be zero and in the range: [1, 268_435_460].
     * <p>
     * WARNING: Do not confuse this with {@link #maximumPacketSize(int)}.
     *
     * @param maximumPacketSize the maximum packet size for sending.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sendMaximumPacketSize(int maximumPacketSize);

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getTopicAliasMaximum() topic alias maximum}.
     * <p>
     * The value must be in the range of an unsigned short: [1, 65_535].
     *
     * @param topicAliasMaximum the topic alias maximum.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B topicAliasMaximum(int topicAliasMaximum);

    /**
     * Sets the {@link Mqtt5ConnectRestrictions#getSendTopicAliasMaximum() topic alias maximum for sending}.
     * <p>
     * The value must be in the range of an unsigned short: [1, 65_535].
     * <p>
     * WARNING: Do not confuse this with {@link #topicAliasMaximum(int)}.
     *
     * @param topicAliasMaximum the topic alias maximum for sending.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sendTopicAliasMaximum(int topicAliasMaximum);

    /**
     * Sets whether {@link Mqtt5ConnectRestrictions#isRequestProblemInformation() problem information is requested}.
     *
     * @param requestProblemInformation whether problem information is requested.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B requestProblemInformation(boolean requestProblemInformation);

    /**
     * Sets whether {@link Mqtt5ConnectRestrictions#isRequestResponseInformation() response information is requested}.
     *
     * @param requestResponseInformation whether response information is requested.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B requestResponseInformation(boolean requestResponseInformation);
}
