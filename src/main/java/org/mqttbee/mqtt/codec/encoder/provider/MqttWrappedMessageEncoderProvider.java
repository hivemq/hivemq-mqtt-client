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
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;
import org.mqttbee.mqtt.netty.NettyThreadLocals;

import java.util.function.Supplier;

/**
 * Provider for a {@link MqttMessageEncoderApplier} for a wrapped MQTT message.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @param <P> the type of the encoder provider for the MQTT message wrapper.
 * @author Silvio Giebl
 */
public interface MqttWrappedMessageEncoderProvider< //
        M extends MqttWrappedMessage<M, W, P>, //
        W extends MqttMessageWrapper<W, M, P>, //
        P extends MqttMessageEncoderProvider<W>> //
        extends Supplier<MqttWrappedMessageEncoderApplier<M, W>> {

    /**
     * @return the encoder provider for the MQTT message wrapper.
     */
    @NotNull
    P getWrapperEncoderProvider();


    /**
     * Provider for a new {@link MqttMessageEncoderApplier} for a wrapped MQTT message.
     *
     * @param <M> the type of the wrapped MQTT message.
     * @param <W> the type of the MQTT message wrapper.
     * @param <P> the type of the encoder provider for the MQTT message wrapper.
     */
    class NewMqttWrappedMessageEncoderProvider< //
            M extends MqttWrappedMessage<M, W, P>, //
            W extends MqttMessageWrapper<W, M, P>, //
            P extends MqttMessageEncoderProvider<W>> //
            implements MqttWrappedMessageEncoderProvider<M, W, P> {

        @NotNull
        public static < //
                M extends MqttWrappedMessage<M, W, MqttMessageEncoderProvider<W>>, //
                W extends MqttMessageWrapper<W, M, MqttMessageEncoderProvider<W>>> //
        MqttWrappedMessageEncoderProvider<M, W, MqttMessageEncoderProvider<W>> create(
                @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider) {

            return new NewMqttWrappedMessageEncoderProvider<>(provider, new MqttMessageWrapperEncoderProvider<>());
        }

        private final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider;
        private final P wrapperEncoderProvider;

        public NewMqttWrappedMessageEncoderProvider(
                @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider,
                @NotNull final P wrapperEncoderProvider) {

            this.provider = provider;
            this.wrapperEncoderProvider = wrapperEncoderProvider;
        }

        @Override
        public MqttWrappedMessageEncoderApplier<M, W> get() {
            return provider.get();
        }

        @NotNull
        public P getWrapperEncoderProvider() {
            return wrapperEncoderProvider;
        }

    }

    /**
     * Provider for a thread local {@link MqttMessageEncoderApplier} for a wrapped MQTT message.
     *
     * @param <M> the type of the wrapped MQTT message.
     * @param <W> the type of the MQTT message wrapper.
     * @param <P> the type of the encoder provider for the MQTT message wrapper.
     */
    class ThreadLocalMqttWrappedMessageEncoderProvider< //
            M extends MqttWrappedMessage<M, W, P>, //
            W extends MqttMessageWrapper<W, M, P>, //
            P extends MqttMessageEncoderProvider<W>> //
            implements MqttWrappedMessageEncoderProvider<M, W, P>, MqttWrappedMessageEncoderApplier<M, W> {

        @NotNull
        public static < //
                M extends MqttWrappedMessage<M, W, MqttMessageEncoderProvider<W>>, //
                W extends MqttMessageWrapper<W, M, MqttMessageEncoderProvider<W>>> //
        MqttWrappedMessageEncoderProvider<M, W, MqttMessageEncoderProvider<W>> create(
                @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider) {

            return new ThreadLocalMqttWrappedMessageEncoderProvider<>(
                    provider, new MqttMessageWrapperEncoderProvider<>());
        }

        private final ThreadLocal<MqttWrappedMessageEncoderApplier<M, W>> threadLocal;
        private final P wrapperEncoderProvider;

        public ThreadLocalMqttWrappedMessageEncoderProvider(
                @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> supplier,
                @NotNull final P wrapperEncoderProvider) {

            threadLocal = ThreadLocal.withInitial(supplier);
            NettyThreadLocals.register(threadLocal);
            this.wrapperEncoderProvider = wrapperEncoderProvider;
        }

        @Override
        public MqttWrappedMessageEncoderApplier<M, W> get() {
            return this;
        }

        @NotNull
        @Override
        public MqttWrappedMessageEncoder<M, W> apply(@NotNull final M message) {
            return threadLocal.get().apply(message);
        }

        @NotNull
        public P getWrapperEncoderProvider() {
            return wrapperEncoderProvider;
        }

    }

}
