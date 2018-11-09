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

package org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback;

import org.jetbrains.annotations.Nullable;

/**
 * SUBACK Return Code according to the MQTT 3.1.1 specification.
 */
public enum Mqtt3SubAckReturnCode {

    SUCCESS_MAXIMUM_QOS_0(0),
    SUCCESS_MAXIMUM_QOS_1(1),
    SUCCESS_MAXIMUM_QOS_2(2),
    FAILURE(128);

    private final int code;

    Mqtt3SubAckReturnCode(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static @Nullable Mqtt3SubAckReturnCode fromCode(final int code) {
        if (code == SUCCESS_MAXIMUM_QOS_0.code) {
            return SUCCESS_MAXIMUM_QOS_0;
        } else if (code == SUCCESS_MAXIMUM_QOS_1.code) {
            return SUCCESS_MAXIMUM_QOS_1;
        } else if (code == SUCCESS_MAXIMUM_QOS_2.code) {
            return SUCCESS_MAXIMUM_QOS_2;
        } else if (code == FAILURE.code) {
            return FAILURE;
        }
        return null;
    }
}