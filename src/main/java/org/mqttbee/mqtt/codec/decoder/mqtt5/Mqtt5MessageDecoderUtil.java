package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.disconnectMalformedUTF8String;
import static org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil.disconnect;

/**
 * @author Silvio Giebl
 */
class Mqtt5MessageDecoderUtil {

    private Mqtt5MessageDecoderUtil() {
    }

    static void disconnectMalformedPropertyLength(@NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length");
    }

    static void disconnectMalformedPropertyIdentifier(@NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed property identifier");
    }

    static void disconnectWrongProperty(@NotNull final Channel channel, @NotNull final String type) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong property for " + type);
    }

    static void disconnectOnlyOnce(@NotNull final Channel channel, @NotNull final String name) {
        disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, name + " must not be included more than once");
    }

    static void disconnectWrongReasonCode(@NotNull final Channel channel, @NotNull final String type) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong reason code for " + type);
    }

    static boolean checkByteOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(channel, name);
            return false;
        }
        if (in.readableBytes() < 1) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length");
            return false;
        }
        return true;
    }

    static boolean checkShortOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (present) {
            disconnectOnlyOnce(channel, name);
            return false;
        }
        if (in.readableBytes() < 2) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length");
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
            disconnectOnlyOnce(channel, name);
            return false;
        }
        if (in.readableBytes() < 4) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed properties length");
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
            disconnectOnlyOnce(channel, name);
            return null;
        }
        final ByteBuffer decoded = MqttBinaryData.decode(in, direct);
        if (decoded == null) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed binary data for " + name);
            return null;
        }
        return decoded;
    }

    @Nullable
    static MqttUTF8StringImpl decodeUTF8StringOnlyOnce(
            @Nullable final MqttUTF8StringImpl current, @NotNull final String name, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        if (current != null) {
            disconnectOnlyOnce(channel, name);
            return null;
        }
        final MqttUTF8StringImpl decoded = MqttUTF8StringImpl.from(in);
        if (decoded == null) {
            disconnectMalformedUTF8String(channel, name);
            return null;
        }
        return decoded;
    }

    @Nullable
    static ImmutableList.Builder<MqttUserPropertyImpl> decodeUserProperty(
            @Nullable ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder, @NotNull final Channel channel,
            @NotNull final ByteBuf in) {

        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(in);
        if (userProperty == null) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed user property");
            return null;
        }
        if (userPropertiesBuilder == null) {
            userPropertiesBuilder = ImmutableList.builder();
        }
        userPropertiesBuilder.add(userProperty);
        return userPropertiesBuilder;
    }

    private static boolean checkProblemInformationRequested(
            @NotNull final String name, @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        if (!clientConnectionData.isProblemInformationRequested()) {
            disconnect(clientConnectionData.getChannel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    name + " must not be included if problem information is not requested");
            return false;
        }
        return true;
    }

    @Nullable
    static MqttUTF8StringImpl decodeReasonStringCheckProblemInformationRequested(
            @Nullable final MqttUTF8StringImpl current,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData, @NotNull final ByteBuf in) {

        if (!checkProblemInformationRequested("reason string", clientConnectionData)) {
            return null;
        }
        return decodeUTF8StringOnlyOnce(current, "reason string", clientConnectionData.getChannel(), in);
    }

    @Nullable
    static ImmutableList.Builder<MqttUserPropertyImpl> decodeUserPropertyCheckProblemInformationRequested(
            @Nullable final ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData, @NotNull final ByteBuf in) {

        if ((userPropertiesBuilder != null) &&
                !checkProblemInformationRequested("user property", clientConnectionData)) {
            return null;
        }
        return decodeUserProperty(userPropertiesBuilder, clientConnectionData.getChannel(), in);
    }

    static boolean checkBoolean(final byte value, @NotNull final String name, @NotNull final Channel channel) {
        if ((value != 0) && (value != 1)) {
            disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "malformed boolean for " + name);
            return false;
        }
        return true;
    }

    static boolean decodeBoolean(final byte value) {
        return value == 1;
    }

}
