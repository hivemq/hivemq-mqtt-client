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

package org.mqttbee.mqtt.codec.decoder;

import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.util.collections.IntMap;

/**
 * @author Silvio Giebl
 */
public class MqttDecoderContext {

    private final int maximumPacketSize;
    private final boolean problemInformationRequested;
    private final boolean responseInformationRequested;
    private final boolean validatePayloadFormat;
    private final boolean directBufferPayload;
    private final boolean directBufferAuth;
    private final boolean directBufferCorrelationData;
    private final @Nullable IntMap<MqttTopicImpl> topicAliasMapping;

    MqttDecoderContext(
            final int maximumPacketSize, final boolean problemInformationRequested,
            final boolean responseInformationRequested, final boolean validatePayloadFormat,
            final boolean directBufferPayload, final boolean directBufferAuth,
            final boolean directBufferCorrelationData, final @Nullable IntMap<MqttTopicImpl> topicAliasMapping) {

        this.maximumPacketSize = maximumPacketSize;
        this.problemInformationRequested = problemInformationRequested;
        this.responseInformationRequested = responseInformationRequested;
        this.validatePayloadFormat = validatePayloadFormat;
        this.directBufferPayload = directBufferPayload;
        this.directBufferAuth = directBufferAuth;
        this.directBufferCorrelationData = directBufferCorrelationData;
        this.topicAliasMapping = topicAliasMapping;
    }

    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    public boolean isResponseInformationRequested() {
        return responseInformationRequested;
    }

    public boolean validatePayloadFormat() {
        return validatePayloadFormat;
    }

    public boolean useDirectBufferPayload() {
        return directBufferPayload;
    }

    public boolean useDirectBufferAuth() {
        return directBufferAuth;
    }

    public boolean useDirectBufferCorrelationData() {
        return directBufferCorrelationData;
    }

    public @Nullable IntMap<MqttTopicImpl> getTopicAliasMapping() {
        return topicAliasMapping;
    }
}
