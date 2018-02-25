package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;

/**
 * @author Silvio Giebl
 */
public class MqttPublishEncoderProvider
        extends MqttMessageWrapperEncoderProvider<MqttPublishWrapper, MqttPublish, MqttPublishEncoderProvider>
        implements MqttMessageEncoderProvider<MqttPublishWrapper> {

    private final MqttMessageEncoderProvider<MqttPubAck> pubAckEncoderProvider;
    private final MqttPubRecEncoderProvider pubRecEncoderProvider;

    public MqttPublishEncoderProvider(
            @NotNull final MqttMessageEncoderProvider<MqttPubAck> pubAckEncoderProvider,
            @NotNull final MqttPubRecEncoderProvider pubRecEncoderProvider) {

        this.pubAckEncoderProvider = pubAckEncoderProvider;
        this.pubRecEncoderProvider = pubRecEncoderProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubAck> getPubAckEncoderProvider() {
        return pubAckEncoderProvider;
    }

    @NotNull
    public MqttPubRecEncoderProvider getPubRecEncoderProvider() {
        return pubRecEncoderProvider;
    }

}
