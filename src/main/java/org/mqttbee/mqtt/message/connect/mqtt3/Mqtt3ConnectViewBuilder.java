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

package org.mqttbee.mqtt.message.connect.mqtt3;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3ConnectViewBuilder<B extends Mqtt3ConnectViewBuilder<B>> {

    private int keepAliveSeconds = Mqtt3Connect.DEFAULT_KEEP_ALIVE;
    private boolean isCleanSession = Mqtt3Connect.DEFAULT_CLEAN_SESSION;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable MqttWillPublish willPublish;

    Mqtt3ConnectViewBuilder() {}

    Mqtt3ConnectViewBuilder(final @NotNull Mqtt3Connect connect) {
        final Mqtt3ConnectView connectView =
                MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt3ConnectView.class);
        keepAliveSeconds = connectView.getKeepAlive();
        isCleanSession = connectView.isCleanSession();
        simpleAuth = connectView.getDelegate().getRawSimpleAuth();
        willPublish = connectView.getDelegate().getRawWillPublish();
    }

    abstract @NotNull B self();

    public @NotNull B keepAlive(final int keepAlive, final @NotNull TimeUnit timeUnit) {
        final long keepAliveSeconds = timeUnit.toSeconds(keepAlive);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAliveSeconds),
                "The value of keep alive converted in seconds must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                keepAliveSeconds, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
        this.keepAliveSeconds = (int) keepAliveSeconds;
        return self();
    }

    public @NotNull B cleanSession(final boolean isCleanSession) {
        this.isCleanSession = isCleanSession;
        return self();
    }

    public @NotNull B simpleAuth(final @Nullable Mqtt3SimpleAuth simpleAuth) {
        this.simpleAuth = (simpleAuth == null) ? null : Mqtt3SimpleAuthView.delegate(simpleAuth);
        return self();
    }

    public @NotNull B willPublish(final @Nullable Mqtt3Publish willPublish) {
        this.willPublish =
                (willPublish == null) ? null : MqttBuilderUtil.willPublish(Mqtt3PublishView.delegate(willPublish));
        return self();
    }

    public @NotNull Mqtt3ConnectView build() {
        return Mqtt3ConnectView.of(keepAliveSeconds, isCleanSession, simpleAuth, willPublish);
    }

    public static class Default extends Mqtt3ConnectViewBuilder<Default> implements Mqtt3ConnectBuilder {

        public Default() { }

        public Default(final @NotNull Mqtt3Connect connect) {
            super(connect);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3ConnectViewBuilder<Nested<P>> implements Mqtt3ConnectBuilder.Nested<P> {

        private final @NotNull Function<? super Mqtt3ConnectView, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3ConnectView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyConnect() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends Mqtt3ConnectViewBuilder<Send<P>> implements Mqtt3ConnectBuilder.Send<P> {

        private final @NotNull Function<? super Mqtt3ConnectView, P> parentConsumer;

        public Send(final @NotNull Function<? super Mqtt3ConnectView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
