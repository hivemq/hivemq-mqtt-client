package org.mqttbee.mqtt.message.publish.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class MqttPubCompBuilder implements Mqtt5PubCompBuilder {

    private final MqttPubRel pubRel;
    private Mqtt5PubCompReasonCode reasonCode = MqttPubComp.DEFAULT_REASON_CODE;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubCompBuilder(@NotNull final MqttPubRel pubRel) {
        this.pubRel = pubRel;
    }

    @NotNull
    @Override
    public MqttPubCompBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public MqttPubCompBuilder withReasonCode(@NotNull final Mqtt5PubCompReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @NotNull
    public MqttPubCompBuilder withReasonString(@Nullable final MqttUTF8StringImpl reasonString) {
        this.reasonString = reasonString;
        return this;
    }

    public MqttPubComp build() {
        return new MqttPubComp(pubRel.getPacketIdentifier(), reasonCode, reasonString, userProperties,
                pubRel.getEncoderProvider().getPubCompEncoderProvider());
    }

}
