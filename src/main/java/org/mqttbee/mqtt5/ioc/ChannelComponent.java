package org.mqttbee.mqtt5.ioc;

import dagger.Subcomponent;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderModule;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt5.handler.auth.Mqtt5AuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5DisconnectOnAuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5ReAuthHandler;
import org.mqttbee.mqtt5.handler.connect.Mqtt5DisconnectOnConnAckHandler;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectHandler;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnecter;
import org.mqttbee.mqtt5.handler.publish.Mqtt5OutgoingQoSHandler;
import org.mqttbee.mqtt5.persistence.Mqtt5PersistenceModule;

/**
 * @author Silvio Giebl
 */
@Subcomponent(modules = {ChannelModule.class, MqttDecoderModule.class, Mqtt5PersistenceModule.class})
@ChannelScope
public interface ChannelComponent {

    AttributeKey<ChannelComponent> KEY = AttributeKey.valueOf("channel.component");

    @NotNull
    static ChannelComponent create(@NotNull final Channel channel, @NotNull final MqttClientData clientData) {
        final ChannelComponent channelComponent =
                MqttBeeComponent.INSTANCE.channelComponent(new ChannelModule(clientData));
        channel.attr(KEY).set(channelComponent);
        return channelComponent;
    }

    @NotNull
    static ChannelComponent get(@NotNull final Channel channel) {
        return channel.attr(KEY).get();
    }

    MqttClientData clientData();

    MqttDecoder decoder();

    MqttEncoder encoder();

    MqttDisconnecter disconnecter();

    Mqtt5DisconnectOnConnAckHandler disconnectOnConnAckHandler();

    Mqtt5AuthHandler authHandler();

    Mqtt5ReAuthHandler reAuthHandler();

    Mqtt5DisconnectOnAuthHandler disconnectOnAuthHandler();

    Mqtt5DisconnectHandler disconnectHandler();

    Mqtt5OutgoingQoSHandler outgoingQoSHandler();

}
