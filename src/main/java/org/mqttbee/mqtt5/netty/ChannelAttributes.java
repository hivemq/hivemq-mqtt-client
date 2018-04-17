/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt5.netty;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class ChannelAttributes {

    private static final AttributeKey<Boolean> SEND_REASON_STRING = AttributeKey.valueOf("reason.string.send");
    private static final boolean SEND_REASON_STRING_DEFAULT = false;

    private static final AttributeKey<Boolean> VALIDATE_PAYLOAD_FORMAT =
            AttributeKey.valueOf("payload.format.indicator.validate");
    private static final boolean VALIDATE_PAYLOAD_FORMAT_DEFAULT = false;

    private static final AttributeKey<Boolean> USE_DIRECT_BUFFER_FOR_PAYLOAD = AttributeKey.valueOf("direct.payload");
    private static final boolean USE_DIRECT_BUFFER_FOR_PAYLOAD_DEFAULT = false;
    private static final AttributeKey<Boolean> USE_DIRECT_BUFFER_FOR_AUTH = AttributeKey.valueOf("direct.auth");
    private static final boolean USE_DIRECT_BUFFER_FOR_AUTH_DEFAULT = false;
    private static final AttributeKey<Boolean> USE_DIRECT_BUFFER_FOR_CORRELATION_DATA =
            AttributeKey.valueOf("direct.correlation.data");
    private static final boolean USE_DIRECT_BUFFER_FOR_CORRELATION_DATA_DEFAULT = false;

    private static boolean get(
            @NotNull final Channel channel, @NotNull final AttributeKey<Boolean> key, final boolean defaultValue) {

        final Boolean value = channel.attr(key).get();
        return (value == null) ? defaultValue : value;
    }

    private static void set(
            @NotNull final Channel channel, @NotNull final AttributeKey<Boolean> key, final boolean value,
            final boolean defaultValue) {

        channel.attr(key).set((value == defaultValue) ? null : value);
    }

    public static boolean sendReasonString(@NotNull final Channel channel) {
        return get(channel, SEND_REASON_STRING, SEND_REASON_STRING_DEFAULT);
    }

    public static void sendReasonString(final boolean send, @NotNull final Channel channel) {
        set(channel, SEND_REASON_STRING, send, SEND_REASON_STRING_DEFAULT);
    }

    public static boolean validatePayloadFormat(@NotNull final Channel channel) {
        return get(channel, VALIDATE_PAYLOAD_FORMAT, VALIDATE_PAYLOAD_FORMAT_DEFAULT);
    }

    public static void validatePayloadFormat(final boolean validate, @NotNull final Channel channel) {
        set(channel, VALIDATE_PAYLOAD_FORMAT, validate, VALIDATE_PAYLOAD_FORMAT_DEFAULT);
    }

    public static boolean useDirectBufferForPayload(@NotNull final Channel channel) {
        return get(channel, USE_DIRECT_BUFFER_FOR_PAYLOAD, USE_DIRECT_BUFFER_FOR_PAYLOAD_DEFAULT);
    }

    public static void useDirectBufferForPayload(final boolean use, @NotNull final Channel channel) {
        set(channel, USE_DIRECT_BUFFER_FOR_PAYLOAD, use, USE_DIRECT_BUFFER_FOR_PAYLOAD_DEFAULT);
    }

    public static boolean useDirectBufferForAuth(@NotNull final Channel channel) {
        return get(channel, USE_DIRECT_BUFFER_FOR_AUTH, USE_DIRECT_BUFFER_FOR_AUTH_DEFAULT);
    }

    public static void useDirectBufferForAuth(final boolean use, @NotNull final Channel channel) {
        set(channel, USE_DIRECT_BUFFER_FOR_AUTH, use, USE_DIRECT_BUFFER_FOR_AUTH_DEFAULT);
    }

    public static boolean useDirectBufferForCorrelationData(@NotNull final Channel channel) {
        return get(channel, USE_DIRECT_BUFFER_FOR_CORRELATION_DATA, USE_DIRECT_BUFFER_FOR_CORRELATION_DATA_DEFAULT);
    }

    public static void useDirectBufferForCorrelationData(final boolean use, @NotNull final Channel channel) {
        set(channel, USE_DIRECT_BUFFER_FOR_CORRELATION_DATA, use, USE_DIRECT_BUFFER_FOR_CORRELATION_DATA_DEFAULT);
    }

    private ChannelAttributes() {
    }

}
