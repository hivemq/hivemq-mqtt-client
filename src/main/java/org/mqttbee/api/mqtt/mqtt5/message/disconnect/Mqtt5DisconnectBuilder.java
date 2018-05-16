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

package org.mqttbee.api.mqtt.mqtt5.message.disconnect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE;
import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectBuilder {

    private boolean withWillMessage = false;
    private long sessionExpiryInterval = MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private MqttUTF8StringImpl serverReference;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5DisconnectBuilder() {
    }

    @NotNull
    public Mqtt5DisconnectBuilder withWillMessage(final boolean withWillMessage) {
        this.withWillMessage = withWillMessage;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withServerReference(@Nullable final String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withServerReference(@Nullable final MqttUTF8String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withReasonString(@Nullable final String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withReasonString(@Nullable final MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5Disconnect build() {
        final Mqtt5DisconnectReasonCode reasonCode =
                withWillMessage ? DISCONNECT_WITH_WILL_MESSAGE : NORMAL_DISCONNECTION;
        return new MqttDisconnect(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties);
    }

}
