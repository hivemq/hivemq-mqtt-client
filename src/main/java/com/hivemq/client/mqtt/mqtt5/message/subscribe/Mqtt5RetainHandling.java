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

package com.hivemq.client.mqtt.mqtt5.message.subscribe;

import org.jetbrains.annotations.Nullable;

/**
 * Retain Handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5RetainHandling {

    /**
     * Send retained messages for the current subscription.
     */
    SEND,
    /**
     * Send retained messages for the current subscription only if the subscription does not currently exist.
     */
    SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
    /**
     * Do not send retained messages for the current subscription.
     */
    DO_NOT_SEND;

    /**
     * @return the byte code of this Retain Handling.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the Retain Handling belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Retain Handling belonging to the byte code or null if the byte code is not a valid Retain Handling.
     */
    public static @Nullable Mqtt5RetainHandling fromCode(final int code) {
        if (code == SEND.getCode()) {
            return SEND;
        } else if (code == SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST.getCode()) {
            return SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        } else if (code == DO_NOT_SEND.getCode()) {
            return DO_NOT_SEND;
        }
        return null;
    }
}
