package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckInternal;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckInternal;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeInternal;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageEncoders {

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnAckImpl> getConnAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PublishInternal> getPublishEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubAckInternal> getPubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRecInternal> getPubRecEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRelInternal> getPubRelEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubCompInternal> getPubCompEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5SubscribeInternal> getSubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5SubAckInternal> getSubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5UnsubscribeInternal> getUnsubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5UnsubAckInternal> getUnsubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingReqImpl> getPingReqEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingRespImpl> getPingRespEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5DisconnectImpl> getDisconnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5AuthImpl> getAuthEncoder();

}
