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

package com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp;

import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of a {@link Mqtt5PubComp MQTT 5 PubComp message}.
 *
 * @author Silvio Giebl
 * @since 1.0
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

    @Override
    public int getCode() {
        return code;
    }

    /**
     * Returns the PubComp Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PubComp Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid PubComp Reason Code.
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
