/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.internal.mqtt.MqttBlockingClient;
import com.hivemq.client.internal.mqtt.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final MqttConnect mqttConnect = MqttChecks.connect(connect);
        try {
            return Mqtt3ConnAckView.of(delegate.connect(mqttConnect));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public @NotNull Mqtt3SubAck subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        try {
            return Mqtt3SubAckView.of(delegate.subscribe(mqttSubscribe));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public @NotNull Mqtt3Publishes publishes(final @Nullable MqttGlobalPublishFilter filter) {
        Checks.notNull(filter, "Global publish filter");

        return new Mqtt3PublishesView(delegate.publishes(filter));
    }

    @Override
    public void unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);
        try {
            delegate.unsubscribe(mqttUnsubscribe);
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public void publish(final @Nullable Mqtt3Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);
        try {
            delegate.publish(mqttPublish);
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            delegate.disconnect(Mqtt3DisconnectView.DELEGATE);
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
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
            } catch (final RuntimeException e) {
                throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
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
            } catch (final RuntimeException e) {
                throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
            }
        }

        @Override
        public @NotNull Optional<Mqtt3Publish> receiveNow() {
            try {
                return delegate.receiveNow().map(Mqtt3PublishView.JAVA_MAPPER);
            } catch (final RuntimeException e) {
                throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
            }
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
