package org.mqttbee.mqtt.message.publish.pubrel;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelBuilder implements Mqtt5PubRelBuilder {

    private final MqttPubRec pubRec;
    private Mqtt5PubRelReasonCode reasonCode = MqttPubRel.DEFAULT_REASON_CODE;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubRelBuilder(@NotNull final MqttPubRec pubRec) {
        this.pubRec = pubRec;
    }

    @NotNull
    @Override
    public MqttPubRelBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public MqttPubRelBuilder withReasonCode(@NotNull final Mqtt5PubRelReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @NotNull
    public MqttPubRelBuilder withReasonString(@Nullable final MqttUTF8StringImpl reasonString) {
        this.reasonString = reasonString;
        return this;
    }

    public MqttPubRel build() {
        return new MqttPubRel(pubRec.getPacketIdentifier(), reasonCode, reasonString, userProperties,
                pubRec.getEncoderProvider().getPubRelEncoderProvider());
    }

}
