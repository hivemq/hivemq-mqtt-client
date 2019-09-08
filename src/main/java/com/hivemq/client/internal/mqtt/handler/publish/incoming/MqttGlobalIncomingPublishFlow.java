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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.util.collections.HandleList.Handle;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow {

    private final @NotNull MqttGlobalPublishFilter filter;
    private @Nullable Handle<MqttGlobalIncomingPublishFlow> handle;

    MqttGlobalIncomingPublishFlow(
            final @NotNull Subscriber<? super Mqtt5Publish> subscriber, final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingQosHandler incomingQosHandler, final @NotNull MqttGlobalPublishFilter filter) {

        super(subscriber, clientConfig, incomingQosHandler);
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

    void setHandle(final @NotNull Handle<MqttGlobalIncomingPublishFlow> handle) {
        this.handle = handle;
    }

    @Nullable Handle<MqttGlobalIncomingPublishFlow> getHandle() {
        return handle;
    }
}
