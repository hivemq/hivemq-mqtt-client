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

import io.reactivex.*;
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
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
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
import org.reactivestreams.Publisher;


/**
 * @author Silvio Giebl
 * @author David Katz
 */
public class Mqtt3ClientView implements Mqtt3Client {

    private final Mqtt5ClientImpl wrapped;

    private static final Function<? super Throwable, ? extends Single<? extends Mqtt5ConnAck>>
        mapMqtt5toMqtt3ExceptionForConnect =
        e -> e instanceof Mqtt5MessageException ? Single.error(Mqtt3ExceptionFactory.map(e)) : Single.error(e);

    private final Function<? super Throwable, ? extends Publisher<? extends Mqtt5PublishResult>>
        mapMqtt5toMqtt3ExceptionsForPublish =
        e -> e instanceof Mqtt5MessageException ? Flowable.error(Mqtt3ExceptionFactory.map(e)) : Flowable.error(e);

    private static final Function<? super Throwable, ? extends Publisher<? extends Mqtt5Publish>>
        mapMqtt5toMqtt3ExceptionsForRemainingPublishes =
        e -> e instanceof Mqtt5MessageException ? Flowable.error(Mqtt3ExceptionFactory.map(e)) : Flowable.error(e);

    private static final Function<? super Throwable, ? extends CompletableSource>
        mapMqtt5toMqtt3ExceptionsForDisconnect =
        e -> e instanceof Mqtt5MessageException ? Completable.error(Mqtt3ExceptionFactory.map(e)) :
            Completable.error(e);

    private static final Function<? super Throwable, ? extends Publisher<? extends Mqtt5Publish>>
        mapMqtt5toMqtt3ExceptionsForAllPublishes =
        e -> e instanceof Mqtt5MessageException ? Flowable.error(Mqtt3ExceptionFactory.map(e)) : Flowable.error(e);

    private static final Function<? super Throwable, ? extends CompletableSource>
        mapMqtt5toMqtt3ExceptionsForUnsubscribe =
        e -> e instanceof Mqtt5MessageException ? Completable.error(Mqtt3ExceptionFactory.map(e)) :
            Completable.error(e);

    public Mqtt3ClientView(@NotNull final Mqtt5ClientImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public Single<Mqtt3ConnAck> connect(@NotNull final Mqtt3Connect connect) {
        final Mqtt3ConnectView connectView =
            MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt3ConnectView.class);
        return wrapped.connect(connectView.getWrapped())
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionForConnect)
            .map(Mqtt3ConnAckView::create);
    }

    @NotNull
    @Override
    public Single<Mqtt3SubAck> subscribe(@NotNull final Mqtt3Subscribe subscribe) {
        final Mqtt3SubscribeView subscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, Mqtt3SubscribeView.class);
        return wrapped.subscribe(subscribeView.getWrapped()).map(Mqtt3SubAckView::create);
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt3SubAck, Mqtt3Publish> subscribeWithStream(@NotNull final Mqtt3Subscribe subscribe) {
        final Mqtt3SubscribeView subscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, Mqtt3SubscribeView.class);
        return wrapped.subscribeWithStream(subscribeView.getWrapped())
                .mapBoth(Mqtt3SubAckView::create, Mqtt3PublishView::create);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3Publish> remainingPublishes() {
        return wrapped.remainingPublishes()
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionsForRemainingPublishes)
            .map(Mqtt3PublishView::create);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3Publish> allPublishes() {
        return wrapped.allPublishes()
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionsForAllPublishes)
            .map(Mqtt3PublishView::create);
    }

    @NotNull
    @Override
    public Completable unsubscribe(@NotNull final Mqtt3Unsubscribe unsubscribe) {
        final Mqtt3UnsubscribeView unsubscribeView =
            MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, Mqtt3UnsubscribeView.class);
        return wrapped.unsubscribe(unsubscribeView.getWrapped())
            .toCompletable()
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionsForUnsubscribe);
    }

    @NotNull
    @Override
    public Flowable<Mqtt3PublishResult> publish(@NotNull final Flowable<Mqtt3Publish> publishFlowable) {
        return wrapped.publish(publishFlowable.map(Mqtt3PublishView::wrapped))
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionsForPublish)
            .map(Mqtt3PublishResultView::create);
    }

    @NotNull
    @Override
    public Completable disconnect() {
        return wrapped.disconnect(Mqtt3DisconnectView.wrapped())
            .onErrorResumeNext(mapMqtt5toMqtt3ExceptionsForDisconnect);
    }

    @NotNull
    @Override
    public Mqtt3ClientData getClientData() {
        return new Mqtt3ClientDataView(wrapped.getClientData());
    }

    public <T> Function<Throwable, Observable<T>> mapException() {
        return t -> t instanceof Mqtt5MessageException ? Observable.error(Mqtt3ExceptionFactory.map(t)) :
            Observable.error(t);
    }
}
