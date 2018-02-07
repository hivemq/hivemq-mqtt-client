package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.connect.Mqtt3ConnectImpl;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;

import javax.inject.Singleton;


@Singleton
public class Mqtt3ConnectEncoder implements Mqtt3MessageEncoder<Mqtt3ConnectImpl> {

    public static final Mqtt3ConnectEncoder INSTANCE = new Mqtt3ConnectEncoder();
    private static final int FIXED_HEADER = Mqtt3MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;
    private static final byte PROTOCOL_VERSION = 4;


    @Override
    public void encode(
            @NotNull final Mqtt3ConnectImpl connect, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encodeFixedHeader(connect, out);
        encodeVariableHeader(connect, out);
        encodePayload(connect, out);

    }


    private void encodeFixedHeader(@NotNull final Mqtt3ConnectImpl connect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(encodedRemainingLength(connect), out);
    }


    private void encodeVariableHeader(
            @NotNull final Mqtt3ConnectImpl connect, @NotNull final ByteBuf out) {

        //protocol name
        Mqtt5UTF8String.PROTOCOL_NAME.to(out);
        //protcol version
        out.writeByte(PROTOCOL_VERSION);
        int connectFlags = 0;

        //username can be set withput password
        if (connect.hasUsername()) {
            connectFlags |= 0b1000_0000;
        }

        // password can only be set, if username is set as well
        if (connect.hasPassword() && connect.hasUsername()) {
            connectFlags |= 0b0100_0000;
        }

        if (connect.hasWill()) {
            final Mqtt3PublishImpl willPublish = connect.getWillPublish();
            //set willflag to true
            connectFlags |= 0b0000_0100;
            //set qos
            connectFlags |= (willPublish.getQos().getCode() << 3);

            if (willPublish.isRetained()) {
                //set will retained
                connectFlags |= 0b0010_0000;
            }
        }
        //clean session
        if (connect.isCleanSession()) {
            connectFlags |= 0b0000_0010;
        }
        out.writeByte(connectFlags);
        out.writeShort(connect.getKeepAlive());
    }


    private void encodePayload(@NotNull final Mqtt3ConnectImpl connect, @NotNull final ByteBuf out) {

        //write clientId;
        connect.getClientId().to(out);
        //write will
        if (connect.hasWill()) {
            encodeWillPublish(connect, out);
        }
        //write username
        if (connect.hasUsername()) {
            connect.getUsername().to(out);
        }
        //write password
        if (connect.hasUsername() && connect.hasPassword()) {
            Mqtt5DataTypes.encodeBinaryData(connect.getPassword(), out);
        }
    }

    private void encodeWillPublish(@NotNull final Mqtt3ConnectImpl connect, @NotNull final ByteBuf out) {
        final Mqtt3PublishImpl willPublish = connect.getWillPublish();
        willPublish.getTopic().to(out);
        final byte[] rawPayload = willPublish.getRawPayload();
        if (rawPayload != null) {
            Mqtt5DataTypes.encodeBinaryData(rawPayload, out);
        }
    }


    public int encodedRemainingLength(@NotNull final Mqtt3ConnectImpl connect) {
        //length variable header, this is fixed
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;
        //length payload
        //client identifier
        remainingLength += connect.getClientId().encodedLength();
        //will
        if (connect.hasWill()) {
            final Mqtt3PublishImpl willPublish = connect.getWillPublish();
            final byte[] nullablePayload = willPublish.getRawPayload();
            //avoid payload being null.
            final byte[] payload;
            if (nullablePayload == null) {
                payload = new byte[0];
            } else {
                payload = nullablePayload;
            }
            if (!Mqtt5DataTypes.isInBinaryDataRange(payload)) {
                throw new Mqtt5BinaryDataExceededException("will payload");
            }
            remainingLength += willPublish.getTopic().encodedLength() + Mqtt5DataTypes.encodedBinaryDataLength(payload);
        }

        //username
        final Mqtt5UTF8String username = connect.getUsername();
        if (username != null) {
            remainingLength += username.encodedLength();
        }
        //password
        final byte[] password = connect.getPassword();
        if (password != null) {
            if (!Mqtt5DataTypes.isInBinaryDataRange(password)) {
                throw new Mqtt5BinaryDataExceededException("password");
            }
            remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(password);
        }


        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }


}
