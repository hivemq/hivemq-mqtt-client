package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * MQTT Topic Filter according to the MQTT 5 specification.
 * <p>
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 * <p>
 * A Topic Filter has the restrictions from {@link Mqtt5UTF8String}, must be at least one character long, may contain
 * one {@link #MULTI_LEVEL_WILDCARD} at the end and may contain multiple {@link #SINGLE_LEVEL_WILDCARD}s.
 *
 * @author Silvio Giebl
 */
public class Mqtt5TopicFilter extends Mqtt5UTF8String {

    /**
     * The multi-level wildcard character.
     */
    public static final char MULTI_LEVEL_WILDCARD = '#';
    /**
     * The single-level wildcard character.
     */
    public static final char SINGLE_LEVEL_WILDCARD = '+';

    static final int WILDCARD_CHECK_FAILURE = -1;
    private static final int WILDCARD_FLAG_MULTI_LEVEL = 1;
    private static final int WILDCARD_FLAG_SINGLE_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_NOT_BEFORE = 0;
    private static final int WILDCARD_CHECK_STATE_BEFORE = 1;
    private static final int WILDCARD_CHECK_STATE_MULTI_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_SINGLE_LEVEL = 3;

    @Nullable
    private static Mqtt5TopicFilter fromInternal(@NotNull final byte[] binary) {
        if ((binary.length == 0) || containsMustNotCharacters(binary)) {
            return null;
        }
        if (Mqtt5SharedTopicFilter.isShared(binary)) {
            return Mqtt5SharedTopicFilter.fromInternal(binary);
        }
        final int wildcardFlags = validateWildcards(binary, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilter(binary, wildcardFlags);
    }

    /**
     * Validates and creates a Topic Filter from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Filter or null if the string is not a valid Topic Filter.
     */
    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final String string) {
        if ((string.length() == 0) || containsMustNotCharacters(string)) {
            return null;
        }
        if (Mqtt5SharedTopicFilter.isShared(string)) {
            return Mqtt5SharedTopicFilter.fromInternal(string);
        }
        final int wildcardFlags = validateWildcards(string, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilter(string, wildcardFlags);
    }

    /**
     * Validates and decodes a Topic Filter from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Topic Filter or null if the byte buffer does not contain a well-formed Topic Filter.
     */
    @Nullable
    public static Mqtt5TopicFilter from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : fromInternal(binary);
    }

    /**
     * Validates the wildcards in the given UTF-8 encoded byte array.
     *
     * @param binary the UTF-8 encoded byte array.
     * @param start  the index in the byte array to start validation at.
     * @return a combination of {@link #WILDCARD_FLAG_MULTI_LEVEL} and {@link #WILDCARD_FLAG_SINGLE_LEVEL} indicating
     * that a multi-level and/or single-level wildcards are present in the byte array or {@link #WILDCARD_CHECK_FAILURE}
     * if the wildcard characters are misplaced
     */
    static int validateWildcards(@NotNull final byte[] binary, final int start) {
        int wildcardFlags = 0;

        int state = WILDCARD_CHECK_STATE_BEFORE;

        for (int i = start; i < binary.length; i++) {
            final byte b = binary[i];
            switch (state) {
                case WILDCARD_CHECK_STATE_NOT_BEFORE:
                    if ((b == SINGLE_LEVEL_WILDCARD) || (b == MULTI_LEVEL_WILDCARD)) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    if (b == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                        state = WILDCARD_CHECK_STATE_BEFORE;
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

    /**
     * Validates the wildcards in the given UTF-16 encoded Java string.
     *
     * @param string the UTF-16 encoded Java string.
     * @param start  the index in the byte array to start validation at.
     * @return a combination of {@link #WILDCARD_FLAG_MULTI_LEVEL} and {@link #WILDCARD_FLAG_SINGLE_LEVEL} indicating
     * that a multi-level and/or single-level wildcards are present in the string or {@link #WILDCARD_CHECK_FAILURE} if
     * the wildcard characters are misplaced
     */
    static int validateWildcards(@NotNull final String string, final int start) {
        int wildcardFlags = 0;

        int state = WILDCARD_CHECK_STATE_BEFORE;

        for (int i = start; i < string.length(); i++) {
            final char c = string.charAt(i);
            switch (state) {
                case WILDCARD_CHECK_STATE_NOT_BEFORE:
                    if ((c == SINGLE_LEVEL_WILDCARD) || (c == MULTI_LEVEL_WILDCARD)) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    if (c == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                        state = WILDCARD_CHECK_STATE_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_BEFORE:
                    switch (c) {
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
                    if (c != Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
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
    public ImmutableList<String> getLevels() {
        return Mqtt5Topic.splitLevels(toString());
    }

    /**
     * @return whether this Topic Filter contains wildcards.
     */
    public boolean containsWildcards() {
        return wildcardFlags != 0;
    }

    /**
     * @return whether this Topic Filter contains a multi-level wildcard.
     */
    public boolean containsMultiLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_MULTI_LEVEL) != 0;
    }

    /**
     * @return whether this Topic Filter contains one ore more single-level wildcards.
     */
    public boolean containsSingleLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_SINGLE_LEVEL) != 0;
    }

    /**
     * @return whether this Topic Filter is shared.
     */
    public boolean isShared() {
        return false;
    }

}
