package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * @author Silvio Giebl
 * @see Mqtt5TopicFilter
 * @see Mqtt5UTF8StringImpl
 */
public class Mqtt5TopicFilterImpl extends Mqtt5UTF8StringImpl implements Mqtt5TopicFilter {

    static final int WILDCARD_CHECK_FAILURE = -1;
    private static final int WILDCARD_FLAG_MULTI_LEVEL = 1;
    private static final int WILDCARD_FLAG_SINGLE_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_NOT_BEFORE = 0;
    private static final int WILDCARD_CHECK_STATE_BEFORE = 1;
    private static final int WILDCARD_CHECK_STATE_MULTI_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_SINGLE_LEVEL = 3;

    /**
     * Validates and decodes a Topic Filter from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created Topic Filter or null if the byte array does not contain a well-formed Topic Filter.
     */
    @Nullable
    public static Mqtt5TopicFilterImpl from(@NotNull final byte[] binary) {
        if ((binary.length == 0) || containsMustNotCharacters(binary)) {
            return null;
        }
        if (Mqtt5SharedTopicFilterImpl.isShared(binary)) {
            return Mqtt5SharedTopicFilterImpl.fromInternal(binary);
        }
        final int wildcardFlags = validateWildcards(binary, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilterImpl(binary, wildcardFlags);
    }

    /**
     * Validates and creates a Topic Filter from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Filter or null if the string is not a valid Topic Filter.
     */
    @Nullable
    public static Mqtt5TopicFilterImpl from(@NotNull final String string) {
        if ((string.length() == 0) || containsMustNotCharacters(string)) {
            return null;
        }
        if (Mqtt5SharedTopicFilterImpl.isShared(string)) {
            return Mqtt5SharedTopicFilterImpl.fromInternal(string);
        }
        final int wildcardFlags = validateWildcards(string, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5TopicFilterImpl(string, wildcardFlags);
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
    public static Mqtt5TopicFilterImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        return (binary == null) ? null : from(binary);
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
                    if (b == Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                        case Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (b != Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                    if (c == Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                        case Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (c != Mqtt5TopicImpl.TOPIC_LEVEL_SEPARATOR) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    state = WILDCARD_CHECK_STATE_BEFORE;
                    break;
            }
        }

        return wildcardFlags;
    }


    private final int wildcardFlags;

    Mqtt5TopicFilterImpl(@NotNull final byte[] binary, final int wildcardFlags) {
        super(binary);
        this.wildcardFlags = wildcardFlags;
    }

    Mqtt5TopicFilterImpl(@NotNull final String string, final int wildcardFlags) {
        super(string);
        this.wildcardFlags = wildcardFlags;
    }

    @NotNull
    @Override
    public ImmutableList<String> getLevels() {
        return Mqtt5TopicImpl.splitLevels(toString());
    }

    @Override
    public boolean containsWildcards() {
        return wildcardFlags != 0;
    }

    @Override
    public boolean containsMultiLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_MULTI_LEVEL) != 0;
    }

    @Override
    public boolean containsSingleLevelWildcard() {
        return (wildcardFlags & WILDCARD_FLAG_SINGLE_LEVEL) != 0;
    }

    @Override
    public boolean isShared() {
        return false;
    }

}
