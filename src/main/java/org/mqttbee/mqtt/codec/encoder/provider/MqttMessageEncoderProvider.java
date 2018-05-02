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
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt.netty.NettyThreadLocals;

import java.util.function.Supplier;

/**
 * Provider for a {@link MqttMessageEncoderApplier}.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public interface MqttMessageEncoderProvider<M extends MqttMessage> extends Supplier<MqttMessageEncoderApplier<M>> {

    @NotNull
    MqttMessageEncoderProvider<? extends MqttMessage> NOT_CODABLE = () -> {
        throw new UnsupportedOperationException();
    };

    @SuppressWarnings("unchecked")
    static <M extends MqttMessage> MqttMessageEncoderProvider<M> notCodable() {
        return (MqttMessageEncoderProvider<M>) NOT_CODABLE;
    }


    /**
     * Provider for a thread local {@link MqttMessageEncoderApplier}.
     *
     * @param <M> the type of the MQTT message.
     */
    class ThreadLocalMqttMessageEncoderProvider<M extends MqttMessage>
            implements MqttMessageEncoderProvider<M>, MqttMessageEncoderApplier<M> {

        private final ThreadLocal<MqttMessageEncoderApplier<M>> threadLocal;

        public ThreadLocalMqttMessageEncoderProvider(
                @NotNull final Supplier<MqttMessageEncoderApplier<M>> supplier) {

            threadLocal = ThreadLocal.withInitial(supplier);
            NettyThreadLocals.register(threadLocal);
        }

        @Override
        public MqttMessageEncoderApplier<M> get() {
            return this;
        }

        @NotNull
        @Override
        public MqttMessageEncoder apply(@NotNull final M message) {
            return threadLocal.get().apply(message);
        }

    }

}
