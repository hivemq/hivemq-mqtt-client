/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;

/**
 * MQTT message with an {@link MqttMessageEncoderApplier} this MQTT message is applied to for encoding.
 *
 * @param <M> the type of the MQTT message.
 * @param <P> the type of the encoder provider for the MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessageWithEncoder<M extends MqttMessageWithEncoder<M, P>, P extends MqttMessageEncoderProvider<M>>
        implements MqttMessage {

    private final P encoderProvider;
    private MqttMessageEncoderApplier<M> encoderApplier;

    MqttMessageWithEncoder(@NotNull final P encoderProvider) {
        this.encoderProvider = encoderProvider;
    }

    @NotNull
    @Override
    public MqttMessageEncoder getEncoder() {
        if (encoderApplier == null) {
            encoderApplier = encoderProvider.get();
        }
        return encoderApplier.apply(getCodable());
    }

    @NotNull
    public P getEncoderProvider() {
        return encoderProvider;
    }

    /**
     * @return the codable MQTT message.
     */
    @NotNull
    protected abstract M getCodable();

}
