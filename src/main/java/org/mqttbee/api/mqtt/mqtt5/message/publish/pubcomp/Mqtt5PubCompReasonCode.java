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

package org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.internal.mqtt.message.MqttCommonReasonCode;

/**
 * MQTT Reason Codes that can be used in PUBCOMP packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PubCompReasonCode implements Mqtt5ReasonCode {

    SUCCESS(MqttCommonReasonCode.SUCCESS),
    PACKET_IDENTIFIER_NOT_FOUND(MqttCommonReasonCode.PACKET_IDENTIFIER_NOT_FOUND);

    private final int code;

    Mqtt5PubCompReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubCompReasonCode(final @NotNull MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this PUBCOMP Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the AUTH Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PUBCOMP Reason Code belonging to the given byte code or null if the byte code is not a valid PUBCOMP
     *         Reason Code code.
     */
    public static @Nullable Mqtt5PubCompReasonCode fromCode(final int code) {
        if (code == SUCCESS.code) {
            return SUCCESS;
        } else if (code == PACKET_IDENTIFIER_NOT_FOUND.code) {
            return PACKET_IDENTIFIER_NOT_FOUND;
        }
        return null;
    }

    @Override
    public boolean canBeSentByClient() {
        return true;
    }
}
