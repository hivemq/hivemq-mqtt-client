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
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.function.Function;

import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE;
import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectBuilder<P> extends FluentBuilder<Mqtt5Disconnect, P> {

    private boolean withWillMessage = false;
    private long sessionExpiryInterval = MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private MqttUTF8StringImpl serverReference;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5DisconnectBuilder(@Nullable final Function<Mqtt5Disconnect, P> parentConsumer) {
        super(parentConsumer);
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> willMessage(final boolean withWillMessage) {
        this.withWillMessage = withWillMessage;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> sessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> serverReference(@Nullable final String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> serverReference(@Nullable final MqttUTF8String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> reasonString(@Nullable final String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> reasonString(@Nullable final MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder<P> userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<? extends Mqtt5DisconnectBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    @Override
    public Mqtt5Disconnect build() {
        final Mqtt5DisconnectReasonCode reasonCode =
                withWillMessage ? DISCONNECT_WITH_WILL_MESSAGE : NORMAL_DISCONNECTION;
        return new MqttDisconnect(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties);
    }

}
