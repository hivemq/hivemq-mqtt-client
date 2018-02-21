package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;

/**
 * @author Silvio Giebl
 */
public class MqttPubRecEncoderProvider implements MqttMessageEncoderProvider<MqttPubRecImpl> {

    private final MqttMessageEncoderProvider<MqttPubRecImpl> provider;
    private final MqttPubRelEncoderProvider pubRelEncoderProvider;

    public MqttPubRecEncoderProvider(
            @NotNull final MqttMessageEncoderProvider<MqttPubRecImpl> provider,
            @NotNull final MqttPubRelEncoderProvider pubRelEncoderProvider) {

        this.provider = provider;
        this.pubRelEncoderProvider = pubRelEncoderProvider;
    }

    @Override
    public MqttMessageEncoderApplier<MqttPubRecImpl> get() {
        return provider.get();
    }

    @NotNull
    public MqttPubRelEncoderProvider getPubRelEncoderProvider() {
        return pubRelEncoderProvider;
    }

}
