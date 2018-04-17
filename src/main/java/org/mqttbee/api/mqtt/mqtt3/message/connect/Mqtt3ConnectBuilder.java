/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.mqtt3.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectBuilder {

    private int keepAlive = Mqtt3Connect.DEFAULT_KEEP_ALIVE;
    private boolean isCleanSession = Mqtt3Connect.DEFAULT_CLEAN_SESSION;
    private MqttSimpleAuth simpleAuth;
    private MqttWillPublish willPublish;

    Mqtt3ConnectBuilder() {
    }

    Mqtt3ConnectBuilder(@NotNull final Mqtt3Connect connect) {
        final Mqtt3ConnectView connectView =
                MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt3ConnectView.class);
        keepAlive = connectView.getKeepAlive();
        isCleanSession = connectView.isCleanSession();
        simpleAuth = connectView.getWrapped().getRawSimpleAuth();
        willPublish = connectView.getWrapped().getRawWillPublish();
    }

    @NotNull
    public Mqtt3ConnectBuilder withKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @NotNull
    public Mqtt3ConnectBuilder withCleanSession(final boolean isCleanSession) {
        this.isCleanSession = isCleanSession;
        return this;
    }

    @NotNull
    public Mqtt3ConnectBuilder withSimpleAuth(@Nullable final Mqtt3SimpleAuth simpleAuth) {
        final Mqtt3SimpleAuthView simpleAuthView =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, Mqtt3SimpleAuthView.class);
        this.simpleAuth = (simpleAuthView == null) ? null : simpleAuthView.getWrapped();
        return this;
    }

    @NotNull
    public Mqtt3ConnectBuilder withWillPublish(@Nullable final Mqtt3Publish willPublish) {
        final Mqtt3PublishView publishView =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, Mqtt3PublishView.class);
        this.willPublish = (publishView == null) ? null : publishView.getWrappedWill();
        return this;
    }

    @NotNull
    public Mqtt3Connect build() {
        return Mqtt3ConnectView.create(keepAlive, isCleanSession, simpleAuth, willPublish);
    }

}
