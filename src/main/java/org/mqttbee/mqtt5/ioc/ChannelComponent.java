package org.mqttbee.mqtt5.ioc;

import dagger.Subcomponent;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.decoder.Mqtt5DecoderModule;
import org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5Decoder;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt5.handler.auth.Mqtt5AuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5DisconnectOnAuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5ReAuthEvent;
import org.mqttbee.mqtt5.handler.auth.Mqtt5ReAuthHandler;
import org.mqttbee.mqtt5.handler.connect.Mqtt5DisconnectOnConnAckHandler;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectHandler;

/**
 * @author Silvio Giebl
 */
@Subcomponent(modules = {ChannelModule.class, Mqtt5DecoderModule.class})
@ChannelScope
public interface ChannelComponent {

    AttributeKey<ChannelComponent> KEY = AttributeKey.valueOf("channel.component");

    @NotNull
    static ChannelComponent forChannel(@NotNull final Channel channel) {
        final Attribute<ChannelComponent> channelComponentAttribute = channel.attr(KEY);
        ChannelComponent channelComponent = channelComponentAttribute.get();
        if (channelComponent == null) {
            channelComponent = MqttBeeComponent.INSTANCE.channelComponent();
            channelComponentAttribute.set(channelComponent);
        }
        return channelComponent;
    }

    Mqtt5Decoder decoder();

    MqttEncoder encoder();

    Mqtt5DisconnectOnConnAckHandler disconnectOnConnAckHandler();

    Mqtt5AuthHandler authHandler();

    Mqtt5ReAuthHandler reAuthHandler();

    Mqtt5DisconnectOnAuthHandler disconnectOnAuthHandler();

    Mqtt5ReAuthEvent reAuthEvent();

    Mqtt5DisconnectHandler disconnectHandler();

}
