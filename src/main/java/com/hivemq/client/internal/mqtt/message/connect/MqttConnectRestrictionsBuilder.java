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

package com.hivemq.client.internal.mqtt.message.connect;

import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictionsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttConnectRestrictionsBuilder<B extends MqttConnectRestrictionsBuilder<B>> {

    private @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum =
            MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM;
    private @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendMaximum =
            MqttConnectRestrictions.DEFAULT_SEND_MAXIMUM;
    private @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize =
            MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE;
    private @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int sendMaximumPacketSize =
            MqttConnectRestrictions.DEFAULT_SEND_MAXIMUM_PACKET_SIZE;
    private @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int topicAliasMaximum =
            MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
    private @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendTopicAliasMaximum =
            MqttConnectRestrictions.DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM;
    private boolean requestProblemInformation = MqttConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION;
    private boolean requestResponseInformation = MqttConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION;

    MqttConnectRestrictionsBuilder() {}

    MqttConnectRestrictionsBuilder(final @NotNull MqttConnectRestrictions restrictions) {
        receiveMaximum = restrictions.getReceiveMaximum();
        sendMaximum = restrictions.getSendMaximum();
        maximumPacketSize = restrictions.getMaximumPacketSize();
        sendMaximumPacketSize = restrictions.getSendMaximumPacketSize();
        topicAliasMaximum = restrictions.getTopicAliasMaximum();
        sendTopicAliasMaximum = restrictions.getSendTopicAliasMaximum();
        requestProblemInformation = restrictions.isRequestProblemInformation();
        requestResponseInformation = restrictions.isRequestResponseInformation();
    }

    abstract @NotNull B self();

    public @NotNull B receiveMaximum(final int receiveMaximum) {
        this.receiveMaximum = Checks.unsignedShortNotZero(receiveMaximum, "Receive maximum");
        return self();
    }

    public @NotNull B sendMaximum(final int sendMaximum) {
        this.sendMaximum = Checks.unsignedShortNotZero(sendMaximum, "Send maximum");
        return self();
    }

    public @NotNull B maximumPacketSize(final int maximumPacketSize) {
        this.maximumPacketSize = MqttChecks.packetSize(maximumPacketSize, "Maximum packet size");
        return self();
    }

    public @NotNull B sendMaximumPacketSize(final int sendMaximumPacketSize) {
        this.sendMaximumPacketSize = MqttChecks.packetSize(sendMaximumPacketSize, "Send maximum packet size");
        return self();
    }

    public @NotNull B topicAliasMaximum(final int topicAliasMaximum) {
        this.topicAliasMaximum = Checks.unsignedShort(topicAliasMaximum, "Topic alias maximum");
        return self();
    }

    public @NotNull B sendTopicAliasMaximum(final int sendTopicAliasMaximum) {
        this.sendTopicAliasMaximum = Checks.unsignedShort(sendTopicAliasMaximum, "Send topic alias maximum");
        return self();
    }

    public @NotNull B requestProblemInformation(final boolean requestProblemInformation) {
        this.requestProblemInformation = requestProblemInformation;
        return self();
    }

    public @NotNull B requestResponseInformation(final boolean requestResponseInformation) {
        this.requestResponseInformation = requestResponseInformation;
        return self();
    }

    public @NotNull MqttConnectRestrictions build() {
        return new MqttConnectRestrictions(receiveMaximum, sendMaximum, maximumPacketSize, sendMaximumPacketSize,
                topicAliasMaximum, sendTopicAliasMaximum, requestProblemInformation, requestResponseInformation);
    }

    public static class Default extends MqttConnectRestrictionsBuilder<Default>
            implements Mqtt5ConnectRestrictionsBuilder {

        public Default() {}

        Default(final @NotNull MqttConnectRestrictions restrictions) {
            super(restrictions);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttConnectRestrictionsBuilder<Nested<P>>
            implements Mqtt5ConnectRestrictionsBuilder.Nested<P> {

        Nested(
                final @NotNull MqttConnectRestrictions restrictions,
                final @NotNull Function<? super MqttConnectRestrictions, P> parentConsumer) {

            super(restrictions);
            this.parentConsumer = parentConsumer;
        }

        private final @NotNull Function<? super MqttConnectRestrictions, P> parentConsumer;

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyRestrictions() {
            return parentConsumer.apply(build());
        }
    }
}
