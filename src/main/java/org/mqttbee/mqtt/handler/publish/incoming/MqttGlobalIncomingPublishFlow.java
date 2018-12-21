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

package org.mqttbee.mqtt.handler.publish.incoming;

import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.util.collections.HandleList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow<Subscriber<? super Mqtt5Publish>> {

    private final @NotNull MqttGlobalPublishFilter filter;
    private @Nullable HandleList.Handle<MqttGlobalIncomingPublishFlow> handle;

    MqttGlobalIncomingPublishFlow(
            final @NotNull Subscriber<? super Mqtt5Publish> subscriber,
            final @NotNull MqttIncomingQosHandler incomingQosHandler, final @NotNull MqttGlobalPublishFilter filter,
            final @NotNull EventLoop eventLoop) {

        super(subscriber, incomingQosHandler, eventLoop);
        this.filter = filter;
    }

    @Override
    void runCancel() {
        incomingQosHandler.getIncomingPublishFlows().cancelGlobal(this);
        super.runCancel();
    }

    @NotNull MqttGlobalPublishFilter getFilter() {
        return filter;
    }

    void setHandle(final @NotNull HandleList.Handle<MqttGlobalIncomingPublishFlow> handle) {
        this.handle = handle;
    }

    @Nullable HandleList.Handle<MqttGlobalIncomingPublishFlow> getHandle() {
        return handle;
    }
}
