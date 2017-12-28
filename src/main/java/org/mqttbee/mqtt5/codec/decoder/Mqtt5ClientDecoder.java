package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientDecoder extends ByteToMessageDecoder {

    private final Mqtt5ConnackDecoder connackDecoder;
    private final Mqtt5PublishDecoder publishDecoder;
    private final Mqtt5PubAckDecoder pubAckDecoder;
    private final Mqtt5PubRecDecoder pubRecDecoder;
    private final Mqtt5PubRelDecoder pubRelDecoder;
    private final Mqtt5PubCompDecoder pubCompDecoder;
    private final Mqtt5SubAckDecoder subAckDecoder;
    private final Mqtt5UnsubAckDecoder unsubAckDecoder;
    private final Mqtt5PingRespDecoder pingRespDecoder;
    private final Mqtt5DisconnectDecoder disconnectDecoder;
    private final Mqtt5AuthDecoder authDecoder;

    @Inject
    public Mqtt5ClientDecoder(
            final Mqtt5ConnackDecoder connackDecoder, final Mqtt5PublishDecoder publishDecoder,
            final Mqtt5PubAckDecoder pubAckDecoder, final Mqtt5PubRecDecoder pubRecDecoder,
            final Mqtt5PubRelDecoder pubRelDecoder, final Mqtt5PubCompDecoder pubCompDecoder,
            final Mqtt5SubAckDecoder subAckDecoder, final Mqtt5UnsubAckDecoder unsubAckDecoder,
            final Mqtt5PingRespDecoder pingRespDecoder, final Mqtt5DisconnectDecoder disconnectDecoder,
            final Mqtt5AuthDecoder authDecoder) {
        this.connackDecoder = connackDecoder;
        this.publishDecoder = publishDecoder;
        this.pubAckDecoder = pubAckDecoder;
        this.pubRecDecoder = pubRecDecoder;
        this.pubRelDecoder = pubRelDecoder;
        this.pubCompDecoder = pubCompDecoder;
        this.subAckDecoder = subAckDecoder;
        this.unsubAckDecoder = unsubAckDecoder;
        this.pingRespDecoder = pingRespDecoder;
        this.disconnectDecoder = disconnectDecoder;
        this.authDecoder = authDecoder;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }
        in.markReaderIndex();

        final byte fixedHeader = in.readByte();
        final int remainingLength = Mqtt5DataTypes.decodeVariableByteInteger(in);

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES) {
            in.resetReaderIndex();
            return;
        }

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE ||
                remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return;
        }

        if (in.readableBytes() < remainingLength) {
            in.resetReaderIndex();
            return;
        }

        final int messageType = (fixedHeader & 0xF0) >> 4;
        final int flags = fixedHeader & 0xF;
        final ByteBuf messageBuffer = in.readSlice(remainingLength);
        in.markReaderIndex();

        Mqtt5Message message = null;
        switch (Mqtt5MessageType.fromCode(messageType)) {
            case RESERVED_ZERO:
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return;
            case CONNECT:
                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                in.clear();
                break;
            case CONNACK:
                message = connackDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PUBLISH:
                message = publishDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PUBACK:
                message = pubAckDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PUBREC:
                message = pubRecDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PUBREL:
                message = pubRelDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PUBCOMP:
                message = pubCompDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case SUBSCRIBE:
                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                in.clear();
                return;
            case SUBACK:
                message = subAckDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case UNSUBSCRIBE:
                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                in.clear();
                return;
            case UNSUBACK:
                message = unsubAckDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case PINGREQ:
                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                in.clear();
                return;
            case PINGRESP:
                message = pingRespDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case DISCONNECT:
                message = disconnectDecoder.decode(flags, remainingLength, messageBuffer);
                break;
            case AUTH:
                message = authDecoder.decode(flags, remainingLength, messageBuffer);
                break;
        }

        if (message != null) {
            out.add(message);
        }
    }

}
