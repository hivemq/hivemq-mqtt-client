/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.internal.mqtt.MqttBlockingClient;
import com.hivemq.client.internal.mqtt.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishResultView;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishViewBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubAckView;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubAck;
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
    public @NotNull Mqtt3ConnAck connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
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
    public Mqtt3ConnectViewBuilder.@NotNull Send<Mqtt3ConnAck> connectWith() {
        return new Mqtt3ConnectViewBuilder.Send<>(this::connect);
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
    public Mqtt3SubscribeViewBuilder.@NotNull Send<Mqtt3SubAck> subscribeWith() {
        return new Mqtt3SubscribeViewBuilder.Send<>(this::subscribe);
    }

    @Override
    public @NotNull PublishesView publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull PublishesView publishes(
            final @Nullable MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        Checks.notNull(filter, "Global publish filter");

        return new PublishesView(delegate.publishes(filter, manualAcknowledgement));
    }

    @Override
    public @NotNull Mqtt3UnsubAck unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);
        try {
            delegate.unsubscribe(mqttUnsubscribe);
            return Mqtt3UnsubAckView.INSTANCE;
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public Mqtt3UnsubscribeViewBuilder.@NotNull Send<Mqtt3UnsubAck> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Send<>(this::unsubscribe);
    }

    @Override
    public @NotNull Mqtt3PublishResult publish(final @Nullable Mqtt3Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);
        try {
            return Mqtt3PublishResultView.of(delegate.publish(mqttPublish));
        } catch (final Mqtt5MessageException e) {
            throw Mqtt3ExceptionFactory.mapWithStackTrace(e);
        }
    }

    @Override
    public Mqtt3PublishViewBuilder.@NotNull Send<Mqtt3PublishResult> publishWith() {
        return new Mqtt3PublishViewBuilder.Send<>(this::publish);
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

    private static class PublishesView implements Publishes {

        private final @NotNull Mqtt5BlockingClient.Publishes delegate;

        PublishesView(final @NotNull Mqtt5BlockingClient.Publishes delegate) {
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
