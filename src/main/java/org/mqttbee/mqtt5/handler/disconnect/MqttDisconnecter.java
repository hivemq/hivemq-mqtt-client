package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * @author Silvio Giebl
 */
public interface MqttDisconnecter {

    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString);

    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode, @NotNull final Throwable cause);

}
