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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3Client;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientData;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishResultView;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;
import org.mqttbee.rx.FlowableWithSingle;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
public class Mqtt3ClientView implements Mqtt3Client {

    private static final Function<Throwable, Completable> EXCEPTION_MAPPER_COMPLETABLE =
            e -> Completable.error(Mqtt3ExceptionFactory.map(e));

    private static final Function<Throwable, Single<Mqtt5ConnAck>> EXCEPTION_MAPPER_SINGLE_CONNACK =
            e -> Single.error(Mqtt3ExceptionFactory.map(e));

    private static final Function<Throwable, Single<Mqtt5SubAck>> EXCEPTION_MAPPER_SINGLE_SUBACK =
            e -> Single.error(Mqtt3ExceptionFactory.map(e));

    private static final Function<Throwable, Flowable<Mqtt5Publish>> EXCEPTION_MAPPER_FLOWABLE_PUBLISH =
            e -> Flowable.error(Mqtt3ExceptionFactory.map(e));

    private static final Function<Throwable, Flowable<Mqtt5PublishResult>> EXCEPTION_MAPPER_FLOWABLE_PUBLISH_RESULT =
            e -> Flowable.error(Mqtt3ExceptionFactory.map(e));

    private final Mqtt5ClientImpl delegate;

    public Mqtt3ClientView(@NotNull final Mqtt5ClientImpl delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public Single<Mqtt3ConnAck> connect(@NotNull final Mqtt3Connect connect) {
        final Mqtt3ConnectView connectView =
                MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt3ConnectView.class);
        return delegate.connect(connectView.getDelegate())
                .onErrorResumeNext(EXCEPTION_MAPPER_SINGLE_CONNACK)
                .map(Mqtt3ConnAckView::of);
    }

    @NotNull
    @Override
    public Single<Mqtt3SubAck> subscribe(@NotNull final Mqtt3Subscribe subscribe) {
        final Mqtt3SubscribeView subscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, Mqtt3SubscribeView.class);
        return delegate.subscribe(subscribeView.getDelegate())
                .onErrorResumeNext(EXCEPTION_MAPPER_SINGLE_SUBACK)
                .map(Mqtt3SubAckView::of);
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt3SubAck, Mqtt3Publish> subscribeWithStream(@NotNull final Mqtt3Subscribe subscribe) {
        final Mqtt3SubscribeView subscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, Mqtt3SubscribeView.class);
        return delegate.subscribeWithStream(subscribeView.getDelegate())
                .mapError(Mqtt3ExceptionFactory.MAPPER)
                .mapBoth(Mqtt3SubAckView::of, Mqtt3PublishView::of);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3Publish> remainingPublishes() {
        return delegate.remainingPublishes()
                .onErrorResumeNext(EXCEPTION_MAPPER_FLOWABLE_PUBLISH)
                .map(Mqtt3PublishView::of);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3Publish> allPublishes() {
        return delegate.allPublishes().onErrorResumeNext(EXCEPTION_MAPPER_FLOWABLE_PUBLISH).map(Mqtt3PublishView::of);
    }

    @NotNull
    @Override
    public Completable unsubscribe(@NotNull final Mqtt3Unsubscribe unsubscribe) {
        final Mqtt3UnsubscribeView unsubscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, Mqtt3UnsubscribeView.class);
        return delegate.unsubscribe(unsubscribeView.getDelegate())
                .toCompletable()
                .onErrorResumeNext(EXCEPTION_MAPPER_COMPLETABLE);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3PublishResult> publish(@NotNull final Flowable<Mqtt3Publish> publishFlowable) {
        return delegate.publish(publishFlowable.map(Mqtt3PublishView::delegate))
                .onErrorResumeNext(EXCEPTION_MAPPER_FLOWABLE_PUBLISH_RESULT)
                .map(Mqtt3PublishResultView::of);
    }

    @NotNull
    @Override
    public Completable disconnect() {
        return delegate.disconnect(Mqtt3DisconnectView.delegate()).onErrorResumeNext(EXCEPTION_MAPPER_COMPLETABLE);
    }

    @NotNull
    @Override
    public Mqtt3ClientData getClientData() {
        return new Mqtt3ClientDataView(delegate.getClientData());
    }

}
