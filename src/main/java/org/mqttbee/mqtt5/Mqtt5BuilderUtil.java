package org.mqttbee.mqtt5;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.*;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.*;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt5BuilderUtil {

    @NotNull
    public static Mqtt5UTF8StringImpl string(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final Mqtt5UTF8StringImpl from = Mqtt5UTF8StringImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid UTF-8 encoded String.");
        }
        return from;
    }

    @NotNull
    public static Mqtt5UTF8StringImpl string(@NotNull final Mqtt5UTF8String string) {
        Preconditions.checkNotNull(string);
        return MustNotBeImplementedUtil.checkNotImplemented(string, Mqtt5UTF8StringImpl.class);
    }

    @Nullable
    public static Mqtt5UTF8StringImpl stringOrNull(@Nullable final String string) {
        return (string == null) ? null : string(string);
    }

    @Nullable
    public static Mqtt5UTF8StringImpl stringOrNull(@Nullable final Mqtt5UTF8String string) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(string, Mqtt5UTF8StringImpl.class);
    }

    @NotNull
    public static Mqtt5TopicImpl topic(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final Mqtt5TopicImpl from = Mqtt5TopicImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Name.");
        }
        return from;
    }

    @NotNull
    public static Mqtt5TopicImpl topic(@NotNull final Mqtt5Topic topic) {
        Preconditions.checkNotNull(topic);
        return MustNotBeImplementedUtil.checkNotImplemented(topic, Mqtt5TopicImpl.class);
    }

    @Nullable
    public static Mqtt5TopicImpl topicOrNull(@Nullable final String string) {
        return (string == null) ? null : topic(string);
    }

    @Nullable
    public static Mqtt5TopicImpl topicOrNull(@Nullable final Mqtt5Topic topic) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(topic, Mqtt5TopicImpl.class);
    }

    @NotNull
    public static Mqtt5TopicFilterImpl topicFilter(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final Mqtt5TopicFilterImpl from = Mqtt5TopicFilterImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Filter.");
        }
        return from;
    }

    @NotNull
    public static Mqtt5TopicFilterImpl topicFilter(@NotNull final Mqtt5TopicFilter topic) {
        Preconditions.checkNotNull(topic);
        return MustNotBeImplementedUtil.checkNotImplemented(topic, Mqtt5TopicFilterImpl.class);
    }

    @NotNull
    public static Mqtt5SharedTopicFilterImpl sharedTopicFilter(
            @NotNull final String shareName, @NotNull final String topicFilter) {

        Preconditions.checkNotNull(shareName);
        Preconditions.checkNotNull(topicFilter);
        final Mqtt5SharedTopicFilterImpl sharedTopicFilter = Mqtt5SharedTopicFilterImpl.from(shareName, topicFilter);
        if (sharedTopicFilter == null) {
            throw new IllegalArgumentException(
                    "The string: [" + Mqtt5SharedTopicFilter.SHARE_PREFIX + shareName + topicFilter +
                            "] is not a valid Shared Topic Filter.");
        }
        return sharedTopicFilter;
    }

    @NotNull
    public static Mqtt5ClientIdentifierImpl clientIdentifier(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final Mqtt5ClientIdentifierImpl from = Mqtt5ClientIdentifierImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Client Identifier.");
        }
        return from;
    }

    @NotNull
    public static Mqtt5ClientIdentifierImpl clientIdentifier(@NotNull final Mqtt5ClientIdentifier clientIdentifier) {
        Preconditions.checkNotNull(clientIdentifier);
        return MustNotBeImplementedUtil.checkNotImplemented(clientIdentifier, Mqtt5ClientIdentifierImpl.class);
    }

    @Nullable
    public static ByteBuffer binaryDataOrNull(@Nullable final byte[] binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(Mqtt5DataTypes.isInBinaryDataRange(binary));
        return ByteBufferUtil.wrap(binary);
    }

    @Nullable
    public static ByteBuffer binaryDataOrNull(@Nullable final ByteBuffer binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(Mqtt5DataTypes.isInBinaryDataRange(binary));
        return ByteBufferUtil.slice(binary);
    }

}
