package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompImpl;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

import java.util.function.Supplier;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelEncoderProvider extends ThreadLocalMqttMessageEncoderProvider<MqttPubRelImpl> {

    private final MqttMessageEncoderProvider<MqttPubCompImpl> pubCompProvider;

    public MqttPubRelEncoderProvider(
            @NotNull final Supplier<MqttMessageEncoderApplier<MqttPubRelImpl>> supplier,
            @NotNull final MqttMessageEncoderProvider<MqttPubCompImpl> pubCompProvider) {

        super(supplier);
        this.pubCompProvider = pubCompProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubCompImpl> getPubCompEncoderProvider() {
        return pubCompProvider;
    }

}
