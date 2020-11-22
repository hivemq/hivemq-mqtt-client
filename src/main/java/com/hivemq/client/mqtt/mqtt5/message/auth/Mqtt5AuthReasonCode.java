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

package com.hivemq.client.mqtt.mqtt5.message.auth;

import com.hivemq.client.internal.mqtt.message.MqttReasonCodes;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of an {@link Mqtt5Auth MQTT 5 Auth message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5AuthReasonCode implements Mqtt5ReasonCode {

    /**
     * Authentication is successful.
     */
    SUCCESS(MqttReasonCodes.SUCCESS),
    /**
     * Continue the authentication with another step.
     */
    CONTINUE_AUTHENTICATION(MqttReasonCodes.CONTINUE_AUTHENTICATION),
    /**
     * Initiate a re-authentication.
     */
    REAUTHENTICATE(MqttReasonCodes.REAUTHENTICATE);

    private final int code;

    Mqtt5AuthReasonCode(final int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    /**
     * Returns the Auth Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Auth Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid Auth Reason Code.
     */
    public static @Nullable Mqtt5AuthReasonCode fromCode(final int code) {
        if (code == SUCCESS.code) {
            return SUCCESS;
        } else if (code == CONTINUE_AUTHENTICATION.code) {
            return CONTINUE_AUTHENTICATION;
        } else if (code == REAUTHENTICATE.code) {
            return REAUTHENTICATE;
        }
        return null;
    }

    @Override
    public boolean canBeSentByServer() {
        return this != REAUTHENTICATE;
    }

    @Override
    public boolean canBeSentByClient() {
        return this != SUCCESS;
    }
}
