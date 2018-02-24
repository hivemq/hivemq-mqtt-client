package org.mqttbee.mqtt.codec.decoder;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.codec.decoder.mqtt3.Mqtt3ClientMessageDecoders;
import org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;

/**
 * @author Silvio Giebl
 */
@Module
public class Mqtt5DecoderModule {

    @Provides
    static MqttMessageDecoders provideMessageDecoders(
            final MqttClientDataImpl clientData, final Lazy<Mqtt5ClientMessageDecoders> mqtt5ClientMessageDecoders,
            final Lazy<Mqtt3ClientMessageDecoders> mqtt3ClientMessageDecoders) {

        switch (clientData.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5ClientMessageDecoders.get();
            case MQTT_3_1_1:
                return mqtt3ClientMessageDecoders.get();
            default:
                throw new IllegalStateException();
        }
    }

}
