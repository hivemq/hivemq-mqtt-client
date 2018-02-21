package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompImpl;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelEncoderProvider implements MqttMessageEncoderProvider<MqttPubRelImpl> {

    private final MqttMessageEncoderProvider<MqttPubRelImpl> provider;
    private final MqttMessageEncoderProvider<MqttPubCompImpl> pubCompProvider;

    public MqttPubRelEncoderProvider(
            @NotNull final MqttMessageEncoderProvider<MqttPubRelImpl> provider,
            @NotNull final MqttMessageEncoderProvider<MqttPubCompImpl> pubCompProvider) {

        this.provider = provider;
        this.pubCompProvider = pubCompProvider;
    }

    @Override
    public MqttMessageEncoderApplier<MqttPubRelImpl> get() {
        return provider.get();
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubCompImpl> getPubCompEncoderProvider() {
        return pubCompProvider;
    }

}
