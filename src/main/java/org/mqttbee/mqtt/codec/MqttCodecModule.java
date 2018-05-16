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

package org.mqttbee.mqtt.codec;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.codec.decoder.mqtt3.Mqtt3ClientMessageDecoders;
import org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3ClientMessageEncoders;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5ClientMessageEncoders;
import org.mqttbee.mqtt.ioc.ChannelScope;

/**
 * @author Silvio Giebl
 */
@Module
public class MqttCodecModule {

    @Provides
    @ChannelScope
    static MqttMessageDecoders provideMessageDecoders(
            final MqttClientData clientData, final Lazy<Mqtt5ClientMessageDecoders> mqtt5ClientMessageDecoders,
            final Lazy<Mqtt3ClientMessageDecoders> mqtt3ClientMessageDecoders) {

        switch (clientData.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5ClientMessageDecoders.get();
            case MQTT_3_1_1:
                return mqtt3ClientMessageDecoders.get();
            default:
                throw new IllegalStateException();
        }
    }

    @Provides
    @ChannelScope
    static MqttMessageEncoders provideMessageEncoders(
            final MqttClientData clientData, final Lazy<Mqtt5ClientMessageEncoders> mqtt5ClientMessageEncoders,
            final Lazy<Mqtt3ClientMessageEncoders> mqtt3ClientMessageEncoders) {

        switch (clientData.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5ClientMessageEncoders.get();
            case MQTT_3_1_1:
                return mqtt3ClientMessageEncoders.get();
            default:
                throw new IllegalStateException();
        }
    }

}
