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

package org.mqttbee.mqtt.mqtt5.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ConnectRestrictionsBuilderBase<B extends Mqtt5ConnectRestrictionsBuilderBase<B>> {

    @NotNull B receiveMaximum(int receiveMaximum);

    @NotNull B sendMaximum(int receiveMaximum);

    @NotNull B maximumPacketSize(int maximumPacketSize);

    @NotNull B sendMaximumPacketSize(int maximumPacketSize);

    @NotNull B topicAliasMaximum(int topicAliasMaximum);

    @NotNull B sendTopicAliasMaximum(int topicAliasMaximum);

    @NotNull B requestProblemInformation(boolean requestProblemInformation);

    @NotNull B sendProblemInformation(boolean requestProblemInformation);

    @NotNull B requestResponseInformation(boolean requestResponseInformation);
}
