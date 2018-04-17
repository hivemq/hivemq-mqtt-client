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

package org.mqttbee.mqtt.message.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

/**
 * @author Silvio Giebl
 */
public class MqttPublishResult implements Mqtt5PublishResult {

    @NotNull
    private final MqttPublish publish;
    @Nullable
    private final Throwable error;

    public MqttPublishResult(@NotNull final MqttPublish publish, @Nullable final Throwable error) {
        this.publish = publish;
        this.error = error;
    }

    @NotNull
    @Override
    public MqttPublish getPublish() {
        return publish;
    }

    @Override
    public boolean isSuccess() {
        return error == null;
    }

    @Nullable
    @Override
    public Throwable getError() {
        return error;
    }


    public static class MqttQoS1Result extends MqttPublishResult implements Mqtt5QoS1Result {

        @NotNull
        private final MqttPubAck pubAck;

        public MqttQoS1Result(
                @NotNull final MqttPublish publish, @Nullable final Throwable error, @NotNull final MqttPubAck pubAck) {

            super(publish, error);
            this.pubAck = pubAck;
        }

        @NotNull
        @Override
        public MqttPubAck getPubAck() {
            return pubAck;
        }

    }


    public static class MqttQoS2Result extends MqttPublishResult implements Mqtt5QoS2Result {

        @NotNull
        private final MqttPubComp pubComp;

        public MqttQoS2Result(
                @NotNull final MqttPublish publish, @Nullable final Throwable error,
                @NotNull final MqttPubComp pubComp) {

            super(publish, error);
            this.pubComp = pubComp;
        }

        @NotNull
        @Override
        public MqttPubComp getPubComp() {
            return pubComp;
        }

    }

}
