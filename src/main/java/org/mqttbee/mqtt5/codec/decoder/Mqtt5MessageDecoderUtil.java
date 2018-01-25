package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * @author Silvio Giebl
 */
class Mqtt5MessageDecoderUtil {

    private Mqtt5MessageDecoderUtil() {
    }

    static void disconnect(
            final Mqtt5DisconnectReasonCode reasonCode, @Nullable final String reasonString,
            @NotNull final Channel channel, @NotNull final ByteBuf in) {

        in.clear();
        channel.config().setAutoRead(false);

        Mqtt5UTF8String mqttReasonString = null;
        if (reasonString != null) {
            final Boolean sendReasonString = channel.attr(ChannelAttributes.SEND_REASON_STRING).get();
            if ((sendReasonString != null) && sendReasonString) {
                mqttReasonString = Mqtt5UTF8String.from(reasonString);
            }
        }

        final Mqtt5DisconnectImpl disconnect = new Mqtt5DisconnectImpl(
                reasonCode, Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, mqttReasonString,
                Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES);
        final ChannelFuture disconnectFuture = channel.writeAndFlush(disconnect);

        disconnectFuture.addListener(ChannelFutureListener.CLOSE);
    }

    static void disconnectWrongFixedHeaderFlags(
            @NotNull final String type, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong fixed header flags for " + type, channel, in);
    }

    static void disconnectRemainingLengthTooShort(@NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "remaining length too short", channel, in);
    }

    static void disconnectMalformedPropertyLength(@NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel, in);
    }

    static void disconnectMalformedPropertyIdentifier(@NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed property identifier", channel, in);
    }

    static void disconnectWrongProperty(
            @NotNull final String type, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong property for " + type, channel, in);
    }

    static void disconnectOnlyOnce(
            @NotNull final String name, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(
                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, name + " must not be included more than once", channel, in);
    }

    static void disconnectMalformedUTF8String(
            @NotNull final String name, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed UTF-8 string for" + name, channel, in);
    }

    static void disconnectWrongReasonCode(
            @NotNull final String type, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong reason code for " + type, channel, in);
    }

    static void disconnectMustNotHavePayload(
            @NotNull final String type, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, type + " must not have a payload", channel, in);
    }

    static boolean checkByteOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(name, channel, in);
            return false;
        }
        if (in.readableBytes() < 1) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel, in);
            return false;
        }
        return true;
    }

    static boolean checkShortOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(name, channel, in);
            return false;
        }
        if (in.readableBytes() < 2) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel, in);
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
            disconnectOnlyOnce(name, channel, in);
            return false;
        }
        if (in.readableBytes() < 4) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length", channel, in);
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
    static byte[] decodeBinaryDataOnlyOnce(
            @Nullable final byte[] current, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (current != null) {
            disconnectOnlyOnce(name, channel, in);
            return null;
        }
        final byte[] decoded = Mqtt5DataTypes.decodeBinaryData(in);
        if (decoded == null) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed binary data for " + name, channel, in);
            return null;
        }
        return decoded;
    }

    @Nullable
    static Mqtt5UTF8String decodeUTF8StringOnlyOnce(
            @Nullable final Mqtt5UTF8String current, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (current != null) {
            disconnectOnlyOnce(name, channel, in);
            return null;
        }
        final Mqtt5UTF8String decoded = Mqtt5UTF8String.from(in);
        if (decoded == null) {
            disconnectMalformedUTF8String(name, channel, in);
            return null;
        }
        return decoded;
    }

    @Nullable
    static ImmutableList.Builder<Mqtt5UserProperty> decodeUserProperty(
            @Nullable ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder,
            @NotNull final Channel channel, @NotNull final ByteBuf in) {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(in);
        if (userProperty == null) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed user property", channel, in);
            return null;
        }
        if (userPropertiesBuilder == null) {
            userPropertiesBuilder = ImmutableList.builder();
        }
        userPropertiesBuilder.add(userProperty);
        return userPropertiesBuilder;
    }

    private static boolean checkProblemInformationRequested(
            @NotNull final String name, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        final Boolean problemInformationRequested = channel.attr(ChannelAttributes.PROBLEM_INFORMATION_REQUESTED).get();
        if ((problemInformationRequested != null) && !problemInformationRequested) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    name + " must not be included if problem information is not requested", channel, in);
            return false;
        }
        return false;
    }

    @Nullable
    static Mqtt5UTF8String decodeReasonStringCheckProblemInformationRequested(
            @Nullable final Mqtt5UTF8String current, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if (!checkProblemInformationRequested("reason string", channel, in)) {
            return null;
        }
        return decodeUTF8StringOnlyOnce(current, "reason string", channel, in);
    }

    @Nullable
    static ImmutableList.Builder<Mqtt5UserProperty> decodeUserPropertyCheckProblemInformationRequested(
            @Nullable final ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder,
            @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if ((userPropertiesBuilder != null) && !checkProblemInformationRequested("user property", channel, in)) {
            return null;
        }
        return decodeUserProperty(userPropertiesBuilder, channel, in);
    }

    static boolean checkBoolean(
            final byte value, @NotNull final String name, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if ((value != 0) && (value != 1)) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "malformed boolean for " + name, channel, in);
            return false;
        }
        return true;
    }

    static boolean decodeBoolean(final byte value) {
        return value == 1;
    }

}
