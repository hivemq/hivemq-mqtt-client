package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.Mqtt5Util;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
class Mqtt5MessageDecoderUtil {

    private Mqtt5MessageDecoderUtil() {
    }

    static void disconnect(
            final Mqtt5DisconnectReasonCode reasonCode, @Nullable final String reasonString,
            @NotNull final Channel channel) {

        Mqtt5Util.disconnect(reasonCode, reasonString, channel);
    }

    static void disconnectWrongFixedHeaderFlags(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong fixed header flags for " + type, channel);
    }

    static void disconnectRemainingLengthTooShort(@NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "remaining length too short", channel);
    }

    static void disconnectMalformedPropertyLength(@NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel);
    }

    static void disconnectMalformedPropertyIdentifier(@NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed property identifier", channel);
    }

    static void disconnectWrongProperty(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong property for " + type, channel);
    }

    static void disconnectOnlyOnce(@NotNull final String name, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, name + " must not be included more than once", channel);
    }

    static void disconnectMalformedUTF8String(@NotNull final String name, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed UTF-8 string for" + name, channel);
    }

    static void disconnectWrongReasonCode(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong reason code for " + type, channel);
    }

    static void disconnectMustNotHavePayload(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, type + " must not have a payload", channel);
    }

    static boolean checkByteOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(name, channel);
            return false;
        }
        if (in.readableBytes() < 1) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel);
            return false;
        }
        return true;
    }

    static boolean checkShortOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(name, channel);
            return false;
        }
        if (in.readableBytes() < 2) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel);
            return false;
        }
        return true;
    }

    static boolean checkShortOnlyOnce(
            final int current, final int notPresent, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        return checkShortOnlyOnce(current != notPresent, name, channel, in);
    }

    static boolean checkIntOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(name, channel);
            return false;
        }
        if (in.readableBytes() < 4) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel);
            return false;
        }
        return true;
    }

    static boolean checkIntOnlyOnce(
            final long current, final long notPresent, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        return checkIntOnlyOnce(current != notPresent, name, channel, in);
    }

    @Nullable
    static ByteBuffer decodeBinaryDataOnlyOnce(
            @Nullable final ByteBuffer current, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in, final boolean direct) {

        if (current != null) {
            disconnectOnlyOnce(name, channel);
            return null;
        }
        final ByteBuffer decoded = Mqtt5DataTypes.decodeBinaryData(in, direct);
        if (decoded == null) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed binary data for " + name, channel);
            return null;
        }
        return decoded;
    }

    @Nullable
    static Mqtt5UTF8StringImpl decodeUTF8StringOnlyOnce(
            @Nullable final Mqtt5UTF8StringImpl current, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (current != null) {
            disconnectOnlyOnce(name, channel);
            return null;
        }
        final Mqtt5UTF8StringImpl decoded = Mqtt5UTF8StringImpl.from(in);
        if (decoded == null) {
            disconnectMalformedUTF8String(name, channel);
            return null;
        }
        return decoded;
    }

    @Nullable
    static ImmutableList.Builder<Mqtt5UserPropertyImpl> decodeUserProperty(
            @Nullable ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder,
            @NotNull final Channel channel, @NotNull final ByteBuf in) {

        final Mqtt5UserPropertyImpl userProperty = Mqtt5UserPropertyImpl.decode(in);
        if (userProperty == null) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed user property", channel);
            return null;
        }
        if (userPropertiesBuilder == null) {
            userPropertiesBuilder = ImmutableList.builder();
        }
        userPropertiesBuilder.add(userProperty);
        return userPropertiesBuilder;
    }

    private static boolean checkProblemInformationRequested(
            @NotNull final String name, @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData) {

        if (!clientConnectionData.isProblemInformationRequested()) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    name + " must not be included if problem information is not requested",
                    clientConnectionData.getChannel());
            return false;
        }
        return true;
    }

    @Nullable
    static Mqtt5UTF8StringImpl decodeReasonStringCheckProblemInformationRequested(
            @Nullable final Mqtt5UTF8StringImpl current,
            @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData, @NotNull final ByteBuf in) {

        if (!checkProblemInformationRequested("reason string", clientConnectionData)) {
            return null;
        }
        return decodeUTF8StringOnlyOnce(current, "reason string", clientConnectionData.getChannel(), in);
    }

    @Nullable
    static ImmutableList.Builder<Mqtt5UserPropertyImpl> decodeUserPropertyCheckProblemInformationRequested(
            @Nullable final ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder,
            @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData, @NotNull final ByteBuf in) {

        if ((userPropertiesBuilder != null) &&
                !checkProblemInformationRequested("user property", clientConnectionData)) {
            return null;
        }
        return decodeUserProperty(userPropertiesBuilder, clientConnectionData.getChannel(), in);
    }

    static boolean checkBoolean(final byte value, @NotNull final String name, @NotNull final Channel channel) {
        if ((value != 0) && (value != 1)) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "malformed boolean for " + name, channel);
            return false;
        }
        return true;
    }

    static boolean decodeBoolean(final byte value) {
        return value == 1;
    }

}
