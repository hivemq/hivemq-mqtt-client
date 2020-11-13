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

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3Message;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MQTT 3 SubAck message. This message is translated from and to an MQTT 3 SUBACK packet.
 *
 * @author Daniel Krüger
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3SubAck extends Mqtt3Message {

    /**
     * @return the Return Codes of this SubAck message, each belonging to a {@link com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription
     *         Subscription} in the corresponding {@link com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe
     *         Subscribe message} in the same order.
     */
    @Immutable @NotNull List<@NotNull Mqtt3SubAckReturnCode> getReturnCodes();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.SUBACK;
    }
}
