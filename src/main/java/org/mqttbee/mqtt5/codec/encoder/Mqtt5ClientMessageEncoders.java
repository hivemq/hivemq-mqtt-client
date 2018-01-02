package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckInternal;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckImpl;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckInternal;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeInternal;

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
    public Mqtt5MessageEncoder<Mqtt5ConnAckImpl> getConnAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send CONNACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PublishInternal> getPublishEncoder() {
        return publishEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubAckInternal> getPubAckEncoder() {
        return pubAckEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRecInternal> getPubRecEncoder() {
        return pubRecEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRelInternal> getPubRelEncoder() {
        return pubRelEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubCompInternal> getPubCompEncoder() {
        return pubCompEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5SubscribeInternal> getSubscribeEncoder() {
        return subscribeEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5SubAckInternal> getSubAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send SUBACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5UnsubscribeInternal> getUnsubscribeEncoder() {
        return unsubscribeEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5UnsubAckInternal> getUnsubAckEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send UNSUBACK packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingReqImpl> getPingReqEncoder() {
        return pingReqEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingRespImpl> getPingRespEncoder() {
        throw new IllegalStateException("A MQTT 5 client does not send PINGRESP packets.");
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5DisconnectImpl> getDisconnectEncoder() {
        return disconnectEncoder;
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5AuthImpl> getAuthEncoder() {
        return authEncoder;
    }

}
