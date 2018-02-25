package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;

import java.util.function.Supplier;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelEncoderProvider extends ThreadLocalMqttMessageEncoderProvider<MqttPubRel> {

    private final MqttMessageEncoderProvider<MqttPubComp> pubCompProvider;

    public MqttPubRelEncoderProvider(
            @NotNull final Supplier<MqttMessageEncoderApplier<MqttPubRel>> supplier,
            @NotNull final MqttMessageEncoderProvider<MqttPubComp> pubCompProvider) {

        super(supplier);
        this.pubCompProvider = pubCompProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubComp> getPubCompEncoderProvider() {
        return pubCompProvider;
    }

}
