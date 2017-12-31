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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ClientMessageEncoders implements Mqtt5MessageEncoders {

    private final Mqtt5ConnectEncoder connectEncoder;
    private final Mqtt5PublishEncoder publishEncoder;
    private final Mqtt5PubAckEncoder pubAckEncoder;
    private final Mqtt5PubRecEncoder pubRecEncoder;
    private final Mqtt5PubRelEncoder pubRelEncoder;
    private final Mqtt5PubCompEncoder pubCompEncoder;
    private final Mqtt5SubscribeEncoder subscribeEncoder;
    private final Mqtt5UnsubscribeEncoder unsubscribeEncoder;
    private final Mqtt5PingReqEncoder pingReqEncoder;
    private final Mqtt5DisconnectEncoder disconnectEncoder;
    private final Mqtt5AuthEncoder authEncoder;

    @Inject
    public Mqtt5ClientMessageEncoders(
            final Mqtt5ConnectEncoder connectEncoder, final Mqtt5PublishEncoder publishEncoder,
            final Mqtt5PubAckEncoder pubAckEncoder, final Mqtt5PubRecEncoder pubRecEncoder,
            final Mqtt5PubRelEncoder pubRelEncoder, final Mqtt5PubCompEncoder pubCompEncoder,
            final Mqtt5SubscribeEncoder subscribeEncoder, final Mqtt5UnsubscribeEncoder unsubscribeEncoder,
            final Mqtt5PingReqEncoder pingReqEncoder, final Mqtt5DisconnectEncoder disconnectEncoder,
            final Mqtt5AuthEncoder authEncoder) {
        this.connectEncoder = connectEncoder;
        this.publishEncoder = publishEncoder;
        this.pubAckEncoder = pubAckEncoder;
        this.pubRecEncoder = pubRecEncoder;
        this.pubRelEncoder = pubRelEncoder;
        this.pubCompEncoder = pubCompEncoder;
        this.subscribeEncoder = subscribeEncoder;
        this.unsubscribeEncoder = unsubscribeEncoder;
        this.pingReqEncoder = pingReqEncoder;
        this.disconnectEncoder = disconnectEncoder;
        this.authEncoder = authEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder() {
        return connectEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5ConnAck> getConnAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send CONNACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PublishImpl> getPublishEncoder() {
        return publishEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubAck> getPubAckEncoder() {
        return pubAckEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRec> getPubRecEncoder() {
        return pubRecEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRel> getPubRelEncoder() {
        return pubRelEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubComp> getPubCompEncoder() {
        return pubCompEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5Subscribe> getSubscribeEncoder() {
        return subscribeEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5SubAck> getSubAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send SUBACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5Unsubscribe> getUnsubscribeEncoder() {
        return unsubscribeEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5UnsubAck> getUnsubAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send UNSUBACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingReq> getPingReqEncoder() {
        return pingReqEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingResp> getPingRespEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send PINGRESP packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5Disconnect> getDisconnectEncoder() {
        return disconnectEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5Auth> getAuthEncoder() {
        return authEncoder;
    }

}
