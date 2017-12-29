package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.connect.Mqtt5Connect;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder {

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final short PROTOCOL_NAME_LENGTH = 4;
    private static final byte[] PROTOCOL_NAME = "MQTT".getBytes(Mqtt5DataTypes.UTF8_STRING_CHARSET);
    private static final byte PROTOCOL_VERSION = 5;

    public void encode(@NotNull final Mqtt5Connect connect, @NotNull final ByteBuf out) {
        encodeFixedHeader(connect, out);
        encodeVariableHeader(connect, out);
        encodePayload(connect, out);
    }

    private void encodeFixedHeader(@NotNull final Mqtt5Connect connect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(calculateRemainingLength(connect), out); // TODO: check size
    }

    private int calculateRemainingLength(@NotNull final Mqtt5Connect connect) {
        return 0;
    }

    private void encodeVariableHeader(@NotNull final Mqtt5Connect connect, @NotNull final ByteBuf out) {
        out.writeShort(PROTOCOL_NAME_LENGTH);
        out.writeBytes(PROTOCOL_NAME);
        out.writeByte(PROTOCOL_VERSION);

        final boolean userNameFlag = false; // TODO
        final boolean passwordFlag = false; // TODO
        final boolean willRetain = false; // TODO
        final byte willQoS = 0; // TODO
        final boolean willFlag = false; // TODO
        final boolean cleanStart = false; // TODO
        int connectFlags = 0;
        if (userNameFlag) {
            connectFlags |= 0b1000_0000;
        }
        if (passwordFlag) {
            connectFlags |= 0b0100_0000;
        }
        if (willRetain) {
            connectFlags |= 0b0010_0000;
        }
        connectFlags |= willQoS << 3;
        if (willFlag) {
            connectFlags |= 0b0000_0100;
        }
        if (cleanStart) {
            connectFlags |= 0b0000_0010;
        }
        out.writeByte(connectFlags);

        final int keepAlive = 0; //TODO
        out.writeShort(keepAlive);

        // Properties
//        SESSION_EXPIRY_INTERVAL
//        AUTHENTICATION_METHOD
//        AUTHENTICATION_DATA
//        REQUEST_PROBLEM_INFORMATION
//        REQUEST_RESPONSE_INFORMATION
//        RECEIVE_MAXIMUM
//        TOPIC_ALIAS_MAXIMUM
//        USER_PROPERTY
//        MAXIMUM_PACKET_SIZE
    }

    private void encodePayload(@NotNull final Mqtt5Connect connect, @NotNull final ByteBuf out) {

    }

}
