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

package org.mqttbee.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3AsyncClient;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3BlockingClient;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3RxClient;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5BlockingClient;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.MqttBlockingClient;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishResultView;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class Mqtt3BlockingClientView implements Mqtt3BlockingClient {

    private final @NotNull MqttBlockingClient delegate;
    private final @NotNull Mqtt3ClientDataView clientData;

    public Mqtt3BlockingClientView(final @NotNull MqttBlockingClient delegate) {
        this.delegate = delegate;
        clientData = new Mqtt3ClientDataView(delegate.getClientData());
    }

    @Override
    public @NotNull Mqtt3ConnAck connect(final @NotNull Mqtt3Connect connect) {
        try {
            return Mqtt3ConnAckView.of(delegate.connect(Mqtt3ConnectView.delegate(connect)));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.map(e);
        }
    }

    @Override
    public @NotNull Mqtt3SubAck subscribe(final @NotNull Mqtt3Subscribe subscribe) {
        try {
            return Mqtt3SubAckView.of(delegate.subscribe(Mqtt3SubscribeView.delegate(subscribe)));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.map(e);
        }
    }

    @Override
    public @NotNull Mqtt3Publishes publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return new Mqtt3PublishesView(delegate.publishes(filter));
    }

    @Override
    public void unsubscribe(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        try {
            delegate.unsubscribe(Mqtt3UnsubscribeView.delegate(unsubscribe));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.map(e);
        }
    }

    @Override
    public @NotNull Mqtt3PublishResult publish(final @NotNull Mqtt3Publish publish) {
        try {
            return Mqtt3PublishResultView.of(delegate.publish(Mqtt3PublishView.delegate(publish)));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.map(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            delegate.disconnect(Mqtt3DisconnectView.delegate());
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.map(e);
        }
    }

    @Override
    public @NotNull Mqtt3ClientData getClientData() {
        return clientData;
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
                throw Mqtt3ExceptionFactory.map(e);
            }
        }

        @Override
        public @NotNull Optional<Mqtt3Publish> receive(
                final long timeout, final @NotNull TimeUnit timeUnit) throws InterruptedException {

            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout must be greater than 0.");
            }
            Objects.requireNonNull(timeUnit, "Time unit must not be null.");

            try {
                return delegate.receive(timeout, timeUnit).map(Mqtt3PublishView.JAVA_MAPPER);
            } catch (final Mqtt5MessageException e) {
                throw Mqtt3ExceptionFactory.map(e);
            }
        }

        @Override
        public @NotNull Optional<Mqtt3Publish> receiveNow() {
            try {
                return delegate.receiveNow().map(Mqtt3PublishView.JAVA_MAPPER);
            } catch (final Mqtt5MessageException e) {
                throw Mqtt3ExceptionFactory.map(e);
            }
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
