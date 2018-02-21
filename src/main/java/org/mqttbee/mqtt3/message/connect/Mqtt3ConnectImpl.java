package org.mqttbee.mqtt3.message.connect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3ConnectEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;

public class Mqtt3ConnectImpl implements Mqtt3Connect, Mqtt3Message {

    private final Mqtt5UTF8StringImpl username;
    private final byte[] password;
    private final Mqtt3PublishImpl willPublish;
    private final boolean cleanSession;
    private final int keepAlive;
    private final Mqtt5ClientIdentifierImpl clientId;

    public Mqtt3ConnectImpl(
            final Mqtt5UTF8StringImpl username, final byte[] password, final Mqtt3PublishImpl willPublish,
            final boolean cleanSession, final int keepAlive, final Mqtt5ClientIdentifierImpl clientId) {
        this.username = username;
        this.password = password;
        this.willPublish = willPublish;
        this.cleanSession = cleanSession;
        this.keepAlive = keepAlive;
        this.clientId = clientId;
    }

    public Mqtt5ClientIdentifierImpl getClientId() {
        return clientId;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public Mqtt5UTF8StringImpl getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    public Mqtt3PublishImpl getWillPublish() {
        return willPublish;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public boolean hasUsername() {
        return this.username != null;
    }

    public boolean hasPassword() {
        return this.password != null;
    }

    public boolean hasWill() {
        return this.willPublish != null;
    }

    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt3ConnectEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength() {
        final int remainingLength = Mqtt3ConnectEncoder.INSTANCE.encodedRemainingLength(this);
        return 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength) + remainingLength;
    }

}
