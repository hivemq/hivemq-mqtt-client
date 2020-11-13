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

package com.hivemq.client.mqtt.mqtt3.message.subscribe;

import com.hivemq.client.mqtt.mqtt3.message.Mqtt3ReturnCode;
import org.jetbrains.annotations.Nullable;

/**
 * Return Code of an {@link Mqtt3SubAck MQTT 3 SubAck message}.
 *
 * @author Daniel Krüger
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt3SubAckReturnCode implements Mqtt3ReturnCode {

    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 0 (this might be a lower QoS than was
     * requested).
     */
    SUCCESS_MAXIMUM_QOS_0(0),
    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 1 (this might be a lower QoS than was
     * requested).
     */
    SUCCESS_MAXIMUM_QOS_1(1),
    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 2.
     */
    SUCCESS_MAXIMUM_QOS_2(2),
    /**
     * The subscription failed.
     */
    FAILURE(128);

    private final int code;

    Mqtt3SubAckReturnCode(final int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean isError() {
        return this == FAILURE;
    }

    /**
     * Returns the SubAck Return Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the SubAck Return Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid SubAck Return Code.
     */
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