package org.mqttbee.api.mqtt5.message.disconnect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE;
import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectBuilder {

    private boolean withWillMessage = false;
    private long sessionExpiryInterval = MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
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
    public Mqtt5DisconnectBuilder withServerReference(@Nullable final Mqtt5UTF8String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withReasonString(@Nullable final String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withReasonString(@Nullable final Mqtt5UTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Disconnect build() {
        final Mqtt5DisconnectReasonCode reasonCode =
                withWillMessage ? DISCONNECT_WITH_WILL_MESSAGE : NORMAL_DISCONNECTION;
        return new MqttDisconnectImpl(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties,
                Mqtt5DisconnectEncoder.PROVIDER);
    }

}
