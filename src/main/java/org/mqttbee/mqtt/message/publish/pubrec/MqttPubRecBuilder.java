package org.mqttbee.mqtt.message.publish.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class MqttPubRecBuilder implements Mqtt5PubRecBuilder {

    private final MqttPublishWrapper publish;
    private Mqtt5PubRecReasonCode reasonCode = MqttPubRec.DEFAULT_REASON_CODE;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubRecBuilder(@NotNull final MqttPublishWrapper publish) {
        this.publish = publish;
    }

    @NotNull
    @Override
    public MqttPubRecBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public MqttPubRecBuilder withReasonCode(@NotNull final Mqtt5PubRecReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @NotNull
    public MqttPubRecBuilder withReasonString(@Nullable final MqttUTF8StringImpl reasonString) {
        this.reasonString = reasonString;
        return this;
    }

    public MqttPubRec build() {
        return new MqttPubRec(publish.getPacketIdentifier(), reasonCode, reasonString, userProperties,
                publish.getEncoderProvider().getPubRecEncoderProvider());
    }

}
