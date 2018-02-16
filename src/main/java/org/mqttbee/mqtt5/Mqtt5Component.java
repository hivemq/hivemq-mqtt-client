package org.mqttbee.mqtt5;

import dagger.Component;
import org.mqttbee.mqtt5.codec.decoder.Mqtt5Decoder;
import org.mqttbee.mqtt5.codec.decoder.Mqtt5DecoderModule;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5Encoder;
import org.mqttbee.mqtt5.handler.Mqtt5DisconnectOnConnAckHandler;
import org.mqttbee.mqtt5.netty.NettyBootstrap;
import org.mqttbee.mqtt5.netty.NettyModule;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Component(modules = {Mqtt5DecoderModule.class, NettyModule.class})
@Singleton
public interface Mqtt5Component {

    Mqtt5Component INSTANCE = DaggerMqtt5Component.create();

    NettyBootstrap nettyBootstrap();

    Mqtt5Decoder decoder();

    Mqtt5Encoder encoder();

    Mqtt5DisconnectOnConnAckHandler disconnectOnConnAckHandler();

}
