package org.mqttbee.mqtt.message.publish.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class MqttPubAckBuilder implements Mqtt5PubAckBuilder {

    private final MqttPublishWrapper publish;
    private Mqtt5PubAckReasonCode reasonCode = MqttPubAck.DEFAULT_REASON_CODE;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubAckBuilder(@NotNull final MqttPublishWrapper publish) {
        this.publish = publish;
    }

    @NotNull
    @Override
    public MqttPubAckBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public MqttPubAckBuilder withReasonCode(@NotNull final Mqtt5PubAckReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @NotNull
    public MqttPubAckBuilder withReasonString(@Nullable final MqttUTF8StringImpl reasonString) {
        this.reasonString = reasonString;
        return this;
    }

    public MqttPubAck build() {
        return new MqttPubAck(publish.getPacketIdentifier(), reasonCode, reasonString, userProperties,
                publish.getEncoderProvider().getPubAckEncoderProvider());
    }

}
