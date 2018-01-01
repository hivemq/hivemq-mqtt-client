package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageEncoders {

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnAckImpl> getConnAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PublishImpl> getPublishEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubAckImpl> getPubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRecImpl> getPubRecEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRelImpl> getPubRelEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubCompImpl> getPubCompEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5SubscribeImpl> getSubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5SubAckImpl> getSubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5UnsubscribeImpl> getUnsubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5UnsubAckImpl> getUnsubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingReqImpl> getPingReqEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingRespImpl> getPingRespEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5DisconnectImpl> getDisconnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5AuthImpl> getAuthEncoder();

}
