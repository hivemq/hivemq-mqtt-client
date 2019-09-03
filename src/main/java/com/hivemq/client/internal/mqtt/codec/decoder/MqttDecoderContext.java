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

package com.hivemq.client.internal.mqtt.codec.decoder;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttDecoderContext {

    private final int maximumPacketSize;
    private final @Nullable MqttTopicImpl @Nullable [] topicAliasMapping;
    private final boolean problemInformationRequested;
    private final boolean responseInformationRequested;
    private final boolean validatePayloadFormat;
    private final boolean directBufferPayload;
    private final boolean directBufferAuth;
    private final boolean directBufferCorrelationData;

    MqttDecoderContext(
            final int maximumPacketSize, final int topicAliasMaximum, final boolean problemInformationRequested,
            final boolean responseInformationRequested, final boolean validatePayloadFormat,
            final boolean directBufferPayload, final boolean directBufferAuth,
            final boolean directBufferCorrelationData) {

        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMapping = (topicAliasMaximum == 0) ? null : new MqttTopicImpl[topicAliasMaximum];
        this.problemInformationRequested = problemInformationRequested;
        this.responseInformationRequested = responseInformationRequested;
        this.validatePayloadFormat = validatePayloadFormat;
        this.directBufferPayload = directBufferPayload;
        this.directBufferAuth = directBufferAuth;
        this.directBufferCorrelationData = directBufferCorrelationData;
    }

    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    public @Nullable MqttTopicImpl @Nullable [] getTopicAliasMapping() {
        return topicAliasMapping;
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
}
