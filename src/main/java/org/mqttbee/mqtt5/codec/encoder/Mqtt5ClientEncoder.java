package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAck;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubComp;
import org.mqttbee.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRec;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRel;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
public class Mqtt5ClientEncoder extends MessageToByteEncoder<Mqtt5Message> {

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
    public Mqtt5ClientEncoder(
            final Mqtt5ConnectEncoder connectEncoder, final Mqtt5PublishEncoder publishEncoder,
            final Mqtt5PubAckEncoder pubAckEncoder, final Mqtt5PubRecEncoder pubRecEncoder,
            final Mqtt5PubRelEncoder pubRelEncoder, final Mqtt5PubCompEncoder pubCompEncoder,
            final Mqtt5SubscribeEncoder subscribeEncoder, final Mqtt5UnsubscribeEncoder unsubscribeEncoder,
            final Mqtt5PingReqEncoder pingReqEncoder, final Mqtt5DisconnectEncoder disconnectEncoder,
            final Mqtt5AuthEncoder authEncoder) {
        super(Mqtt5Message.class, true);
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

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt5Message message, final ByteBuf out)
            throws Exception {
        if (message instanceof Mqtt5Connect) {
            connectEncoder.encode((Mqtt5Connect) message, out);
        } else if (message instanceof Mqtt5Publish) {
            publishEncoder.encode((Mqtt5Publish) message, out);
        } else if (message instanceof Mqtt5PubAck) {
            pubAckEncoder.encode((Mqtt5PubAck) message, out);
        } else if (message instanceof Mqtt5PubRec) {
            pubRecEncoder.encode((Mqtt5PubRec) message, out);
        } else if (message instanceof Mqtt5PubRel) {
            pubRelEncoder.encode((Mqtt5PubRel) message, out);
        } else if (message instanceof Mqtt5PubComp) {
            pubCompEncoder.encode((Mqtt5PubComp) message, out);
        } else if (message instanceof Mqtt5Subscribe) {
            subscribeEncoder.encode((Mqtt5Subscribe) message, out);
        } else if (message instanceof Mqtt5Unsubscribe) {
            unsubscribeEncoder.encode((Mqtt5Unsubscribe) message, out);
        } else if (message instanceof Mqtt5PingReq) {
            pingReqEncoder.encode((Mqtt5PingReq) message, out);
        } else if (message instanceof Mqtt5Disconnect) {
            disconnectEncoder.encode((Mqtt5Disconnect) message, out);
        } else if (message instanceof Mqtt5Auth) {
            authEncoder.encode((Mqtt5Auth) message, out);
        }
    }

}
