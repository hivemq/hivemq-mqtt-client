package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;

import java.util.function.Supplier;

/**
 * @author Silvio Giebl
 */
public class MqttPubRecEncoderProvider extends ThreadLocalMqttMessageEncoderProvider<MqttPubRec> {

    private final MqttPubRelEncoderProvider pubRelEncoderProvider;

    public MqttPubRecEncoderProvider(
            @NotNull final Supplier<MqttMessageEncoderApplier<MqttPubRec>> supplier,
            @NotNull final MqttPubRelEncoderProvider pubRelEncoderProvider) {

        super(supplier);
        this.pubRelEncoderProvider = pubRelEncoderProvider;
    }

    @NotNull
    public MqttPubRelEncoderProvider getPubRelEncoderProvider() {
        return pubRelEncoderProvider;
    }

}
