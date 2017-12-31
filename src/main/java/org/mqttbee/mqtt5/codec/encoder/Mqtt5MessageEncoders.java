package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAck;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubComp;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRec;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRel;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAck;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageEncoders {

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5ConnAck> getConnAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PublishImpl> getPublishEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubAck> getPubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRec> getPubRecEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubRel> getPubRelEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PubComp> getPubCompEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5Subscribe> getSubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5SubAck> getSubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5Unsubscribe> getUnsubscribeEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5UnsubAck> getUnsubAckEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingReq> getPingReqEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5PingResp> getPingRespEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5Disconnect> getDisconnectEncoder();

    @NotNull
    Mqtt5MessageEncoder<Mqtt5Auth> getAuthEncoder();

}
