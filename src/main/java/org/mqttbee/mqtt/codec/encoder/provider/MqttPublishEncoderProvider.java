package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublishImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;

/**
 * @author Silvio Giebl
 */
public class MqttPublishEncoderProvider
        extends MqttMessageWrapperEncoderProvider<MqttPublishWrapper, MqttPublishImpl, MqttPublishEncoderProvider>
        implements MqttMessageEncoderProvider<MqttPublishWrapper> {

    private final MqttMessageEncoderProvider<MqttPubAckImpl> pubAckEncoderProvider;
    private final MqttPubRecEncoderProvider pubRecEncoderProvider;

    public MqttPublishEncoderProvider(
            @NotNull final MqttMessageEncoderProvider<MqttPubAckImpl> pubAckEncoderProvider,
            @NotNull final MqttPubRecEncoderProvider pubRecEncoderProvider) {

        this.pubAckEncoderProvider = pubAckEncoderProvider;
        this.pubRecEncoderProvider = pubRecEncoderProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubAckImpl> getPubAckEncoderProvider() {
        return pubAckEncoderProvider;
    }

    @NotNull
    public MqttPubRecEncoderProvider getPubRecEncoderProvider() {
        return pubRecEncoderProvider;
    }

}
