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

package com.hivemq.mqtt.client2.internal.mqtt3;

import com.hivemq.mqtt.client2.MqttGlobalPublishFilter;
import com.hivemq.mqtt.client2.internal.MqttRxClient;
import com.hivemq.mqtt.client2.internal.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.mqtt.client2.internal.message.disconnect.mqtt3.Mqtt3DisconnectView;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishResultView;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubscribe;
import com.hivemq.mqtt.client2.internal.message.subscribe.mqtt3.Mqtt3SubAckView;
import com.hivemq.mqtt.client2.internal.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.internal.util.MqttChecks;
import com.hivemq.mqtt.client2.libs.rxjava3.FlowableWithSingle;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3RxClient;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.mqtt.client2.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.mqtt.client2.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.mqtt.client2.mqtt3.message.unsubscribe.Mqtt3UnsubAck;
import com.hivemq.mqtt.client2.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.mqtt.client2.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.mqtt.client2.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.mqtt.client2.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
public class Mqtt3RxClientView implements Mqtt3RxClient {

    private static final @NotNull Function<Mqtt3Publish, MqttPublish> PUBLISH_MAPPER = MqttChecks::publish;

    private static final @NotNull Function<Throwable, Completable> EXCEPTION_MAPPER_COMPLETABLE =
            e -> Completable.error(Mqtt3ExceptionFactory.map(e));

    private static final @NotNull Function<Throwable, Single<Mqtt5ConnAck>> EXCEPTION_MAPPER_SINGLE_CONNACK =
            e -> Single.error(Mqtt3ExceptionFactory.map(e));

    private static final @NotNull Function<Throwable, Single<Mqtt5SubAck>> EXCEPTION_MAPPER_SINGLE_SUBACK =
            e -> Single.error(Mqtt3ExceptionFactory.map(e));

    private static final @NotNull Function<Throwable, Single<Mqtt5UnsubAck>> EXCEPTION_MAPPER_SINGLE_UNSUBACK =
            e -> Single.error(Mqtt3ExceptionFactory.map(e));

    private static final @NotNull Function<Throwable, Flowable<Mqtt5Publish>> EXCEPTION_MAPPER_FLOWABLE_PUBLISH =
            e -> Flowable.error(Mqtt3ExceptionFactory.map(e));

    private static final @NotNull Function<Throwable, Flowable<Mqtt5PublishResult>>
            EXCEPTION_MAPPER_FLOWABLE_PUBLISH_RESULT = e -> Flowable.error(Mqtt3ExceptionFactory.map(e));

    private final @NotNull MqttRxClient delegate;
    private final @NotNull Mqtt3ClientConfigView clientConfig;

    Mqtt3RxClientView(final @NotNull MqttRxClient delegate) {
        this.delegate = delegate;
        clientConfig = new Mqtt3ClientConfigView(delegate.getConfig());
    }

    @Override
    public @NotNull Single<Mqtt3ConnAck> connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
    }

    @Override
    public @NotNull Single<Mqtt3ConnAck> connect(final @Nullable Mqtt3Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);

        return delegate.connect(mqttConnect)
                .onErrorResumeNext(EXCEPTION_MAPPER_SINGLE_CONNACK)
                .map(Mqtt3ConnAckView.MAPPER);
    }

    @Override
    public Mqtt3ConnectViewBuilder.@NotNull Nested<Single<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(this::connect);
    }

    @Override
    public @NotNull Single<Mqtt3SubAck> subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return delegate.subscribe(mqttSubscribe)
                .onErrorResumeNext(EXCEPTION_MAPPER_SINGLE_SUBACK)
                .map(Mqtt3SubAckView.MAPPER);
    }

    @Override
    public Mqtt3SubscribeViewBuilder.@NotNull Nested<Single<Mqtt3SubAck>> subscribeWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribe);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribePublishes(
            final @Nullable Mqtt3Subscribe subscribe) {
        return subscribePublishes(subscribe, false);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribePublishes(
            final @Nullable Mqtt3Subscribe subscribe,
            final boolean manualAcknowledgement) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return delegate.subscribePublishes(mqttSubscribe, manualAcknowledgement)
                .mapError(Mqtt3ExceptionFactory.MAPPER)
                .mapBoth(Mqtt3PublishView.MAPPER, Mqtt3SubAckView.MAPPER);
    }

    @Override
    public @NotNull Mqtt3SubscribeViewPublishesBuilder subscribePublishesWith() {
        return new Mqtt3SubscribeViewPublishesBuilder();
    }

    @Override
    public @NotNull Flowable<Mqtt3Publish> publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull Flowable<Mqtt3Publish> publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final boolean manualAcknowledgement) {
        Checks.notNull(filter, "Global publish filter");

        return delegate.publishes(filter, manualAcknowledgement)
                .onErrorResumeNext(EXCEPTION_MAPPER_FLOWABLE_PUBLISH)
                .map(Mqtt3PublishView.MAPPER);
    }

    @Override
    public @NotNull Single<Mqtt3UnsubAck> unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);

        return delegate.unsubscribe(mqttUnsubscribe)
                .onErrorResumeNext(EXCEPTION_MAPPER_SINGLE_UNSUBACK)
                .map(Mqtt3UnsubAckView.MAPPER);
    }

    @Override
    public Mqtt3UnsubscribeViewBuilder.@NotNull Nested<Single<Mqtt3UnsubAck>> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Nested<>(this::unsubscribe);
    }

    @Override
    public @NotNull Flowable<Mqtt3PublishResult> publish(final @NotNull Publisher<Mqtt3Publish> publisher) {
        Checks.notNull(publisher, "Publisher");

        return delegate.publish(publisher, PUBLISH_MAPPER)
                .onErrorResumeNext(EXCEPTION_MAPPER_FLOWABLE_PUBLISH_RESULT)
                .map(Mqtt3PublishResultView.MAPPER);
    }

    @Override
    public @NotNull Completable disconnect() {
        return delegate.disconnect(Mqtt3DisconnectView.DELEGATE).onErrorResumeNext(EXCEPTION_MAPPER_COMPLETABLE);
    }

    @Override
    public @NotNull Mqtt3ClientConfigView getConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull Mqtt3AsyncClientView toAsync() {
        return new Mqtt3AsyncClientView(delegate.toAsync());
    }

    @Override
    public @NotNull Mqtt3BlockingClientView toBlocking() {
        return new Mqtt3BlockingClientView(delegate.toBlocking());
    }

    public class Mqtt3SubscribeViewPublishesBuilder
            extends Mqtt3SubscribeViewBuilder.Publishes<FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck>> {

        @Override
        public @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> applySubscribe() {
            return subscribePublishes(build(), manualAcknowledgement);
        }
    }
}
