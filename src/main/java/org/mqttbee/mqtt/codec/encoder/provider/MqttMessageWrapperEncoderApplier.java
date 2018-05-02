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
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;
import org.mqttbee.mqtt.netty.NettyThreadLocals;

import java.util.function.Supplier;

/**
 * Applies a {@link MqttMessageWrapper} to a {@link MqttMessageEncoder}.
 *
 * @param <W> the type of the MQTT message wrapper.
 * @param <M> the type of the wrapped MQTT message.
 * @param <E> the type of the encoder of the wrapped MQTT message.
 * @author Silvio Giebl
 */
public interface MqttMessageWrapperEncoderApplier< //
        W extends MqttMessageWrapper<W, M, ?>, //
        M extends MqttWrappedMessage<M, W, ?>, //
        E extends MqttWrappedMessageEncoder<M, W>> {

    /**
     * Returns a encoder for a MQTT message wrapper that is applied to the given MQTT message wrapper.
     *
     * @param message        the MQTT message wrapper.
     * @param wrappedEncoder the encoder of the wrapped MQTT message.
     * @return the encoder for the MQTT message wrapper.
     */
    @NotNull
    MqttMessageEncoder apply(@NotNull W message, @NotNull E wrappedEncoder);


    /**
     * Applies a {@link MqttMessageWrapper} to thread local {@link MqttMessageEncoder}.
     *
     * @param <W> the type of the MQTT message wrapper.
     * @param <M> the type of the wrapped MQTT message.
     * @param <E> the type of the encoder of the wrapped MQTT message.
     */
    class ThreadLocalMqttMessageWrapperEncoderApplier< //
            W extends MqttMessageWrapper<W, M, ?>, //
            M extends MqttWrappedMessage<M, W, ?>, //
            E extends MqttWrappedMessageEncoder<M, W>> //
            implements MqttMessageWrapperEncoderApplier<W, M, E> {

        private final ThreadLocal<MqttMessageWrapperEncoderApplier<W, M, E>> threadLocal;

        public ThreadLocalMqttMessageWrapperEncoderApplier(
                @NotNull final Supplier<MqttMessageWrapperEncoderApplier<W, M, E>> supplier) {

            threadLocal = ThreadLocal.withInitial(supplier);
            NettyThreadLocals.register(threadLocal);
        }

        @NotNull
        @Override
        public MqttMessageEncoder apply(@NotNull final W message, @NotNull final E wrappedEncoder) {
            return threadLocal.get().apply(message, wrappedEncoder);
        }

    }

}
