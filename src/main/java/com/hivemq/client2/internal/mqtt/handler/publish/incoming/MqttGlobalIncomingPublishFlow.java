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

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.util.collections.HandleList.Handle;
import com.hivemq.client2.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow {

    private final @NotNull MqttGlobalPublishFilter filter;
    private @Nullable Handle<MqttGlobalIncomingPublishFlow> handle;

    MqttGlobalIncomingPublishFlow(
            final @NotNull Subscriber<? super Mqtt5Publish> subscriber,
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingQosHandler incomingQosHandler,
            final @NotNull MqttGlobalPublishFilter filter,
            final boolean manualAcknowledgement) {

        super(subscriber, clientConfig, incomingQosHandler, manualAcknowledgement);
        this.filter = filter;
    }

    @Override
    void runCancel() {
        incomingPublishService.incomingPublishFlows.cancelGlobal(this);
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
