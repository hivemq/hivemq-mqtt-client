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

package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;

/**
 * @author Silvio Giebl
 */
public class MqttPublishEncoderProvider
        extends MqttMessageWrapperEncoderProvider<MqttPublishWrapper, MqttPublish, MqttPublishEncoderProvider>
        implements MqttMessageEncoderProvider<MqttPublishWrapper> {

    private final MqttMessageEncoderProvider<MqttPubAck> pubAckEncoderProvider;
    private final MqttPubRecEncoderProvider pubRecEncoderProvider;

    public MqttPublishEncoderProvider(
            @NotNull final MqttMessageEncoderProvider<MqttPubAck> pubAckEncoderProvider,
            @NotNull final MqttPubRecEncoderProvider pubRecEncoderProvider) {

        this.pubAckEncoderProvider = pubAckEncoderProvider;
        this.pubRecEncoderProvider = pubRecEncoderProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubAck> getPubAckEncoderProvider() {
        return pubAckEncoderProvider;
    }

    @NotNull
    public MqttPubRecEncoderProvider getPubRecEncoderProvider() {
        return pubRecEncoderProvider;
    }

}
