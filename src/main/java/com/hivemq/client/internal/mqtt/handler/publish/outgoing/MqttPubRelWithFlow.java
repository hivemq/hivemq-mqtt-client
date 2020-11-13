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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.mqtt.message.publish.MqttPubRec;
import com.hivemq.client.internal.mqtt.message.publish.MqttPubRel;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * @author Silvio Giebl
 */
abstract class MqttPubRelWithFlow extends MqttPubOrRelWithFlow {

    private final @NotNull MqttPubRel pubRel;

    MqttPubRelWithFlow(final @NotNull MqttPubRel pubRel, final @NotNull MqttAckFlow ackFlow) {
        super(ackFlow);
        this.pubRel = pubRel;
    }

    @NotNull MqttPubRel getPubRel() {
        return pubRel;
    }

    static class MqttQos2IntermediateWithFlow extends MqttPubRelWithFlow implements BooleanSupplier {

        private int state;

        MqttQos2IntermediateWithFlow(
                final @NotNull MqttPubRel pubRel, final @NotNull MqttAckFlow ackFlow) {

            super(pubRel, ackFlow);
        }

        @Override
        public boolean getAsBoolean() {
            return ++state == 2;
        }
    }

    static class MqttQos2CompleteWithFlow extends MqttPubRelWithFlow {

        private final @NotNull MqttPublish publish;
        private final @NotNull MqttPubRec pubRec;

        MqttQos2CompleteWithFlow(
                final @NotNull MqttPublish publish,
                final @NotNull MqttPubRec pubRec,
                final @NotNull MqttPubRel pubRel,
                final @NotNull MqttAckFlow ackFlow) {

            super(pubRel, ackFlow);
            this.publish = publish;
            this.pubRec = pubRec;
        }

        @NotNull MqttPublish getPublish() {
            return publish;
        }

        @NotNull MqttPubRec getPubRec() {
            return pubRec;
        }
    }
}
