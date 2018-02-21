package org.mqttbee.mqtt.codec.decoder;

import dagger.Module;
import dagger.Provides;
import org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;

/**
 * @author Silvio Giebl
 */
@Module
public class Mqtt5DecoderModule {

    @Provides
    static MqttMessageDecoders provideMessageDecoders(final Mqtt5ClientMessageDecoders clientMessageDecoders) {
        return clientMessageDecoders;
    }

}
