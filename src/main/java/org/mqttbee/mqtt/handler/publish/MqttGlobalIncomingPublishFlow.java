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

package org.mqttbee.mqtt.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFlowType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.util.collections.ScNodeList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow<Subscriber<? super Mqtt5Publish>> {

    private final MqttGlobalPublishFlowType type;
    private ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle;

    MqttGlobalIncomingPublishFlow(
            @NotNull final Subscriber<? super Mqtt5Publish> subscriber,
            @NotNull final MqttIncomingPublishService incomingPublishService,
            @NotNull final MqttGlobalPublishFlowType type) {

        super(incomingPublishService, subscriber);
        this.type = type;
    }

    @Override
    void runRemoveOnCancel() {
        incomingPublishService.getIncomingPublishFlows().cancelGlobal(this);
    }

    @NotNull
    public MqttGlobalPublishFlowType getType() {
        return type;
    }

    void setHandle(@NotNull final ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle) {
        this.handle = handle;
    }

    ScNodeList.Handle<MqttGlobalIncomingPublishFlow> getHandle() {
        return handle;
    }

}
