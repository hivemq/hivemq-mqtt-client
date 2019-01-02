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

package org.mqttbee.internal.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.MqttBlockingClient;
import org.mqttbee.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.internal.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import org.mqttbee.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.internal.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;
import org.mqttbee.internal.mqtt.util.MqttChecks;
import org.mqttbee.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.mqtt.mqtt3.Mqtt3AsyncClient;
import org.mqttbee.mqtt.mqtt3.Mqtt3BlockingClient;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientConfig;
import org.mqttbee.mqtt.mqtt3.Mqtt3RxClient;
import org.mqttbee.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.mqtt.mqtt5.Mqtt5BlockingClient;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.util.AsyncRuntimeException;
import org.mqttbee.util.Checks;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class Mqtt3BlockingClientView implements Mqtt3BlockingClient {

    private final @NotNull MqttBlockingClient delegate;
    private final @NotNull Mqtt3ClientConfigView clientConfig;

    Mqtt3BlockingClientView(final @NotNull MqttBlockingClient delegate) {
        this.delegate = delegate;
        clientConfig = new Mqtt3ClientConfigView(delegate.getConfig());
    }

    @Override
    public @NotNull Mqtt3ConnAck connect(final @Nullable Mqtt3Connect connect) {
        try {
            return Mqtt3ConnAckView.of(delegate.connect(MqttChecks.connect(connect)));
        } catch (final Mqtt5MessageException e) {
            throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
        }
    }

    @Override
    public @NotNull Mqtt3SubAck subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        try {
            return Mqtt3SubAckView.of(delegate.subscribe(MqttChecks.subscribe(subscribe)));
        } catch (final Mqtt5MessageException e) {
            throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
        }
    }

    @Override
    public @NotNull Mqtt3Publishes publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return new Mqtt3PublishesView(delegate.publishes(filter));
    }

    @Override
    public void unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        try {
            delegate.unsubscribe(MqttChecks.unsubscribe(unsubscribe));
        } catch (final Mqtt5MessageException e) {
            throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
        }
    }

    @Override
    public void publish(final @Nullable Mqtt3Publish publish) {
        try {
            delegate.publish(MqttChecks.publish(publish));
        } catch (final Mqtt5MessageException e) {
            throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
        }
    }

    @Override
    public void disconnect() {
        try {
            delegate.disconnect(Mqtt3DisconnectView.DELEGATE);
        } catch (final Mqtt5MessageException e) {
            throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
        }
    }

    @Override
    public @NotNull Mqtt3ClientConfig getConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull Mqtt3RxClient toRx() {
        return new Mqtt3RxClientView(delegate.toRx());
    }

    @Override
    public @NotNull Mqtt3AsyncClient toAsync() {
        return new Mqtt3AsyncClientView(delegate.toAsync());
    }

    private static class Mqtt3PublishesView implements Mqtt3Publishes {

        private final @NotNull Mqtt5BlockingClient.Mqtt5Publishes delegate;

        Mqtt3PublishesView(final @NotNull Mqtt5BlockingClient.Mqtt5Publishes delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NotNull Mqtt3Publish receive() throws InterruptedException {
            try {
                return Mqtt3PublishView.of(delegate.receive());
            } catch (final Mqtt5MessageException e) {
                throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
            }
        }

        @Override
        public @NotNull Optional<Mqtt3Publish> receive(
                final long timeout, final @Nullable TimeUnit timeUnit) throws InterruptedException {

            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout must be greater than 0.");
            }
            Checks.notNull(timeUnit, "Time unit");

            try {
                return delegate.receive(timeout, timeUnit).map(Mqtt3PublishView.JAVA_MAPPER);
            } catch (final Mqtt5MessageException e) {
                throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
            }
        }

        @Override
        public @NotNull Optional<Mqtt3Publish> receiveNow() {
            try {
                return delegate.receiveNow().map(Mqtt3PublishView.JAVA_MAPPER);
            } catch (final Mqtt5MessageException e) {
                throw AsyncRuntimeException.fillInStackTrace(Mqtt3ExceptionFactory.map(e));
            }
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
