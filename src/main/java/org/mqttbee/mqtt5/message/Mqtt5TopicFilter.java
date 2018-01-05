package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicFilter extends Mqtt5UTF8String {

    public static final byte MULTI_LEVEL_WILDCARD = '#';
    public static final byte SINGLE_LEVEL_WILDCARD = '+';

    static final int WILDCARD_CHECK_FAILURE = -1;
    private static final int WILDCARD_FLAG_MULTI_LEVEL = 1;
    private static final int WILDCARD_FLAG_SINGLE_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_NOT_BEFORE = 0;
    private static final int WILDCARD_CHECK_STATE_BEFORE = 1;
    private static final int WILDCARD_CHECK_STATE_MULTI_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_SINGLE_LEVEL = 3;

    @Nullable
    static Mqtt5TopicFilter fromInternal(@NotNull final byte[] binary) {
        if ((binary.length == 0) || containsMustNotCharacters(binary)) {
            return null;
        }
        if (Mqtt5SharedTopicFilter.isShared(binary)) {
            return Mqtt5SharedTopicFilter.fromInternal(binary);
        }
        final int wildcardFlags = checkWildcards(binary, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilter(binary, wildcardFlags);
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final String string) {
        if ((string.length() == 0) || containsMustNotCharacters(string)) {
            return null;
        }
        if (Mqtt5SharedTopicFilter.isShared(string)) {
            return Mqtt5SharedTopicFilter.fromInternal(string);
        }
        final int wildcardFlags = checkWildcards(string, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilter(string, wildcardFlags);
    }

    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : fromInternal(binary);
    }

    static int checkWildcards(@NotNull final byte[] binary, final int start) {
        int wildcardFlags = 0;

        int state = WILDCARD_CHECK_STATE_BEFORE;

        for (final byte b : binary) {
            switch (state) {
                case WILDCARD_CHECK_STATE_NOT_BEFORE:
                    if ((b == SINGLE_LEVEL_WILDCARD) || (b == MULTI_LEVEL_WILDCARD)) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_BEFORE:
                    switch (b) {
                        case MULTI_LEVEL_WILDCARD:
                            wildcardFlags |= WILDCARD_FLAG_MULTI_LEVEL;
                            state = WILDCARD_CHECK_STATE_MULTI_LEVEL;
                            break;
                        case SINGLE_LEVEL_WILDCARD:
                            wildcardFlags |= WILDCARD_FLAG_SINGLE_LEVEL;
                            state = WILDCARD_CHECK_STATE_SINGLE_LEVEL;
                            break;
                        case Mqtt5Topic.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (b != Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    state = WILDCARD_CHECK_STATE_BEFORE;
                    break;
            }
        }

        return wildcardFlags;
    }

    static int checkWildcards(@NotNull final String string, final int start) {
        int wildcardFlags = 0;

        int state = WILDCARD_CHECK_STATE_BEFORE;

        for (int i = 0; i < string.length(); i++) {
            final char b = string.charAt(i);
            switch (state) {
                case WILDCARD_CHECK_STATE_NOT_BEFORE:
                    if ((b == SINGLE_LEVEL_WILDCARD) || (b == MULTI_LEVEL_WILDCARD)) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_BEFORE:
                    switch (b) {
                        case MULTI_LEVEL_WILDCARD:
                            wildcardFlags |= WILDCARD_FLAG_MULTI_LEVEL;
                            state = WILDCARD_CHECK_STATE_MULTI_LEVEL;
                            break;
                        case SINGLE_LEVEL_WILDCARD:
                            wildcardFlags |= WILDCARD_FLAG_SINGLE_LEVEL;
                            state = WILDCARD_CHECK_STATE_SINGLE_LEVEL;
                            break;
                        case Mqtt5Topic.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (b != Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    state = WILDCARD_CHECK_STATE_BEFORE;
                    break;
            }
        }

        return wildcardFlags;
    }


    private final int wildcardFlags;

    Mqtt5TopicFilter(@NotNull final byte[] binary, final int wildcardFlags) {
        super(binary);
        this.wildcardFlags = wildcardFlags;
    }

    Mqtt5TopicFilter(@NotNull final String string, final int wildcardFlags) {
        super(string);
        this.wildcardFlags = wildcardFlags;
    }

    @NotNull
    public String[] getLevels() {
        return toString().split(Character.toString((char) Mqtt5Topic.TOPIC_LEVEL_SEPARATOR));
    }

    public boolean containsWildcards() {
        return wildcardFlags != 0;
    }

    public boolean containsSingleLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_SINGLE_LEVEL) != 0;
    }

    public boolean containsMultiLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_MULTI_LEVEL) != 0;
    }

    public boolean isShared() {
        return false;
    }

}
