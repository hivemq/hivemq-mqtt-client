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

package org.mqttbee.api.mqtt.mqtt3.message.connect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectBuilder<P> extends FluentBuilder<Mqtt3Connect, P> {

    private int keepAliveSeconds = Mqtt3Connect.DEFAULT_KEEP_ALIVE;
    private boolean isCleanSession = Mqtt3Connect.DEFAULT_CLEAN_SESSION;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable MqttWillPublish willPublish;

    public Mqtt3ConnectBuilder(final @Nullable Function<? super Mqtt3Connect, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt3ConnectBuilder(final @NotNull Mqtt3Connect connect) {
        super(null);
        final Mqtt3ConnectView connectView =
                MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt3ConnectView.class);
        keepAliveSeconds = connectView.getKeepAlive();
        isCleanSession = connectView.isCleanSession();
        simpleAuth = connectView.getDelegate().getRawSimpleAuth();
        willPublish = connectView.getDelegate().getRawWillPublish();
    }

    public @NotNull Mqtt3ConnectBuilder<P> keepAlive(final int keepAlive, final @NotNull TimeUnit timeUnit) {
        final long keepAliveSeconds = timeUnit.toSeconds(keepAlive);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAliveSeconds),
                "The value of keep alive converted in seconds must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                keepAliveSeconds, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
        this.keepAliveSeconds = (int) keepAliveSeconds;
        return this;
    }

    public @NotNull Mqtt3ConnectBuilder<P> cleanSession(final boolean isCleanSession) {
        this.isCleanSession = isCleanSession;
        return this;
    }

    public @NotNull Mqtt3ConnectBuilder<P> simpleAuth(final @Nullable Mqtt3SimpleAuth simpleAuth) {
        final Mqtt3SimpleAuthView simpleAuthView =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, Mqtt3SimpleAuthView.class);
        this.simpleAuth = (simpleAuthView == null) ? null : simpleAuthView.getDelegate();
        return this;
    }

    public @NotNull Mqtt3SimpleAuthBuilder<? extends Mqtt3ConnectBuilder<P>> simpleAuth() {
        return new Mqtt3SimpleAuthBuilder<>(this::simpleAuth);
    }

    public @NotNull Mqtt3ConnectBuilder<P> willPublish(final @Nullable Mqtt3Publish willPublish) {
        final Mqtt3PublishView publishView =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, Mqtt3PublishView.class);
        this.willPublish = (publishView == null) ? null : publishView.getWillDelegate();
        return this;
    }

    public @NotNull Mqtt3PublishBuilder<? extends Mqtt3ConnectBuilder<P>> willPublish() {
        return new Mqtt3PublishBuilder<>(this::willPublish);
    }

    @Override
    public @NotNull Mqtt3Connect build() {
        return Mqtt3ConnectView.of(keepAliveSeconds, isCleanSession, simpleAuth, willPublish);
    }

    public @NotNull P applyConnect() {
        return apply();
    }

}
