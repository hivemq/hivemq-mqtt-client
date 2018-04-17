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

package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.util.collections.ScNodeList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow {

    public static final int TYPE_ALL_SUBSCRIPTIONS = 0;
    public static final int TYPE_ALL_PUBLISHES = 1;
    public static final int TYPE_REMAINING_PUBLISHES = 2;
    public static final int TYPE_COUNT = 3;

    private final int type;
    private ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle;

    MqttGlobalIncomingPublishFlow(
            @NotNull final Subscriber<? super Mqtt5SubscribeResult> actual,
            @NotNull final MqttIncomingPublishService incomingPublishService, final int type) {

        super(actual, incomingPublishService);
        this.type = type;
    }

    @Override
    void runRemoveOnCancel() {
        incomingPublishService.getIncomingPublishFlows().cancelGlobal(this);
    }

    public int getType() {
        return type;
    }

    public void setHandle(@NotNull final ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle) {
        this.handle = handle;
    }

    public ScNodeList.Handle<MqttGlobalIncomingPublishFlow> getHandle() {
        return handle;
    }

}
