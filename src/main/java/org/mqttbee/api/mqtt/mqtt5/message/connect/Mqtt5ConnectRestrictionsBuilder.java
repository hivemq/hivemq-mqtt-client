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

package org.mqttbee.api.mqtt.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectRestrictionsBuilder<P> extends FluentBuilder<Mqtt5ConnectRestrictions, P> {

    private int receiveMaximum = Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM;
    private int topicAliasMaximum = Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
    private int maximumPacketSize = Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;

    public Mqtt5ConnectRestrictionsBuilder(
            @Nullable final Function<? super Mqtt5ConnectRestrictions, P> parentConsumer) {

        super(parentConsumer);
    }

    @NotNull
    public Mqtt5ConnectRestrictionsBuilder<P> receiveMaximum(final int receiveMaximum) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(receiveMaximum),
                "The value of receive maximum must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                receiveMaximum, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);

        this.receiveMaximum = receiveMaximum;
        return this;
    }

    @NotNull
    public Mqtt5ConnectRestrictionsBuilder<P> topicAliasMaximum(final int topicAliasMaximum) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(topicAliasMaximum),
                "The value of topic alias maximum must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                topicAliasMaximum, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);

        this.topicAliasMaximum = topicAliasMaximum;
        return this;
    }

    @NotNull
    public Mqtt5ConnectRestrictionsBuilder<P> maximumPacketSize(final int maximumPacketSize) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(maximumPacketSize),
                "The value of maximum packet size must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                maximumPacketSize, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);
        this.maximumPacketSize = maximumPacketSize;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ConnectRestrictions build() {
        return new MqttConnectRestrictions(receiveMaximum, topicAliasMaximum, maximumPacketSize);
    }

}
