package org.mqttbee.mqtt5.ioc;

import dagger.Component;
import org.mqttbee.mqtt5.handler.Mqtt5ChannelInitializerProvider;
import org.mqttbee.mqtt5.netty.NettyBootstrap;
import org.mqttbee.mqtt5.netty.NettyModule;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Component(modules = {NettyModule.class})
@Singleton
public interface MqttBeeComponent {

    MqttBeeComponent INSTANCE = DaggerMqttBeeComponent.create();

    NettyBootstrap nettyBootstrap();

    Mqtt5ChannelInitializerProvider channelInitializerProvider();

    ChannelComponent.Builder channelComponentBuilder();

}
