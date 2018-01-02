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
public abstract class Mqtt5TestMessageEncoders implements Mqtt5MessageEncoders {

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5ConnectImpl> getConnectEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5ConnAckImpl> getConnAckEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PublishInternal> getPublishEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubAckInternal> getPubAckEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRecInternal> getPubRecEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubRelInternal> getPubRelEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PubCompInternal> getPubCompEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5SubscribeInternal> getSubscribeEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5SubAckInternal> getSubAckEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5UnsubscribeInternal> getUnsubscribeEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5UnsubAckInternal> getUnsubAckEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingReqImpl> getPingReqEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5PingRespImpl> getPingRespEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5DisconnectImpl> getDisconnectEncoder() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Mqtt5MessageEncoder<Mqtt5AuthImpl> getAuthEncoder() {
        throw new UnsupportedOperationException();
    }
    
}
