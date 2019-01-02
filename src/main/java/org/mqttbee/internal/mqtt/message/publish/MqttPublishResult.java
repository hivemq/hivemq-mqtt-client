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

package org.mqttbee.internal.mqtt.message.publish;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.internal.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.internal.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.internal.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRel;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * @author Silvio Giebl
 */
public class MqttPublishResult implements Mqtt5PublishResult {

    private final @NotNull MqttPublish publish;
    private final @Nullable Throwable error;

    public MqttPublishResult(final @NotNull MqttPublish publish, final @Nullable Throwable error) {
        this.publish = publish;
        this.error = error;
    }

    @Override
    public @NotNull MqttPublish getPublish() {
        return publish;
    }

    @Override
    public @NotNull Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public boolean acknowledged() {
        return true;
    }

    public static class MqttQos1Result extends MqttPublishResult implements Mqtt5Qos1Result {

        private final @NotNull MqttPubAck pubAck;

        public MqttQos1Result(
                final @NotNull MqttPublish publish, final @Nullable Throwable error, final @NotNull MqttPubAck pubAck) {

            super(publish, error);
            this.pubAck = pubAck;
        }

        @Override
        public @NotNull MqttPubAck getPubAck() {
            return pubAck;
        }
    }

    public static class MqttQos2Result extends MqttPublishResult implements Mqtt5Qos2Result {

        private final @NotNull MqttPubRec pubRec;

        public MqttQos2Result(
                final @NotNull MqttPublish publish, final @Nullable Throwable error, final @NotNull MqttPubRec pubRec) {

            super(publish, error);
            this.pubRec = pubRec;
        }

        @Override
        public @NotNull MqttPubRec getPubRec() {
            return pubRec;
        }
    }

    public static class MqttQos2IntermediateResult extends MqttQos2Result {

        private final @NotNull BooleanSupplier acknowledgedCallback;

        public MqttQos2IntermediateResult(
                final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec,
                final @NotNull BooleanSupplier acknowledgedCallback) {

            super(publish, null, pubRec);
            this.acknowledgedCallback = acknowledgedCallback;
        }

        public boolean acknowledged() {
            return acknowledgedCallback.getAsBoolean();
        }
    }

    public static class MqttQos2CompleteResult extends MqttQos2Result implements Mqtt5Qos2CompleteResult {

        private final @NotNull MqttPubRel pubRel;
        private final @NotNull MqttPubComp pubComp;

        public MqttQos2CompleteResult(
                final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec, final @NotNull MqttPubRel pubRel,
                final @NotNull MqttPubComp pubComp) {

            super(publish, null, pubRec);
            this.pubRel = pubRel;
            this.pubComp = pubComp;
        }

        @Override
        public @NotNull MqttPubRel getPubRel() {
            return pubRel;
        }

        @Override
        public @NotNull MqttPubComp getPubComp() {
            return pubComp;
        }
    }

}
