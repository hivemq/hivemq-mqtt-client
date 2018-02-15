package org.mqttbee.mqtt5.codec.decoder;

import dagger.Module;
import dagger.Provides;

/**
 * @author Silvio Giebl
 */
@Module
public class Mqtt5DecoderModule {

    @Provides
    static Mqtt5MessageDecoders provideMessageDecoders(final Mqtt5ClientMessageDecoders clientMessageDecoders) {
        return clientMessageDecoders;
    }

}
