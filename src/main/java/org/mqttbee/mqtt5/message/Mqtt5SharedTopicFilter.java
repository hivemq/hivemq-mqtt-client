package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * MQTT Shared Topic Filter according to the MQTT 5 specification.
 * <p>
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 * <p>
 * A Shared Topic Filter consists of the {@link #SHARE_PREFIX} a Share Name and a Topic Filter. The Share Name has the
 * restrictions from {@link Mqtt5UTF8String}, must be at least 1 character long and mut not contain wildcards ({@link
 * Mqtt5TopicFilter#MULTI_LEVEL_WILDCARD}, {@link Mqtt5TopicFilter#SINGLE_LEVEL_WILDCARD}) and the {@link
 * Mqtt5Topic#TOPIC_LEVEL_SEPARATOR}. The Topic Filter has the same restrictions from {@link Mqtt5TopicFilter}.
 *
 * @author Silvio Giebl
 */
public class Mqtt5SharedTopicFilter extends Mqtt5TopicFilter {

    /**
     * The prefix of a Shared Topic Filter.
     */
    public static final String SHARE_PREFIX = "$share" + Mqtt5Topic.TOPIC_LEVEL_SEPARATOR;
    private static final int SHARE_PREFIX_LENGTH = SHARE_PREFIX.length();

    /**
     * Checks whether the given UTF-8 encoded byte array represents a Shared Topic Filter. This method does not validate
     * whether it represents a valid Shared Topic Filter but only whether it starts with {@link #SHARE_PREFIX}.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return whether the byte array represents a Shared Topic Filter.
     */
    static boolean isShared(@NotNull final byte[] binary) {
        if (binary.length < SHARE_PREFIX_LENGTH) {
            return false;
        }
        for (int i = 0; i < SHARE_PREFIX_LENGTH; i++) {
            if (binary[i] != SHARE_PREFIX.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given UTF-16 encoded Java string represents a Shared Topic Filter. This method does not
     * validate whether it represents a valid Shared Topic Filter but only whether it starts with {@link
     * #SHARE_PREFIX}.
     *
     * @param string the given UTF-16 encoded Java string.
     * @return whether the string represents a Shared Topic Filter.
     */
    static boolean isShared(@NotNull final String string) {
        return string.startsWith(SHARE_PREFIX);
    }

    /**
     * Validates and creates a Shared Topic Filter from the given byte array.
     * <p>
     * This method does not validate {@link Mqtt5TopicFilter#containsMustNotCharacters(byte[])} and {@link
     * #isShared(byte[])}.
     *
     * @param binary the UTF-8 encoded byte array staring with {@link #SHARE_PREFIX}.
     * @return the created Shared Topic Filter or null if the byte array is not a valid Shared Topic Filter.
     */
    @Nullable
    static Mqtt5SharedTopicFilter fromInternal(@NotNull final byte[] binary) {
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        while (shareNameEnd < binary.length) {
            final byte b = binary[shareNameEnd];
            if (b == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                break;
            }
            if ((b == MULTI_LEVEL_WILDCARD) || (b == SINGLE_LEVEL_WILDCARD)) {
                return null;
            }
            shareNameEnd++;
        }
        if ((shareNameEnd == SHARE_PREFIX_LENGTH) || (shareNameEnd >= binary.length - 1)) {
            return null;
        }
        final int wildcardFlags = validateWildcards(binary, shareNameEnd + 1);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(binary, wildcardFlags);
    }

    /**
     * Validates and creates a Shared Topic Filter from the given string.
     * <p>
     * This method does not validate {@link Mqtt5TopicFilter#containsMustNotCharacters(byte[])} and {@link
     * #isShared(byte[])}.
     *
     * @param string the UTF-16 encoded Java string staring with {@link #SHARE_PREFIX}.
     * @return the created Shared Topic Filter or null if the string is not a valid Shared Topic Filter.
     */
    @Nullable
    static Mqtt5SharedTopicFilter fromInternal(@NotNull final String string) {
        // no isShared and containsMustNotCharacters, already checked in TopicFilter
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        while (shareNameEnd < string.length()) {
            final char c = string.charAt(shareNameEnd);
            if (c == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                break;
            }
            if ((c == MULTI_LEVEL_WILDCARD) || (c == SINGLE_LEVEL_WILDCARD)) {
                return null;
            }
            shareNameEnd++;
        }
        if ((shareNameEnd == SHARE_PREFIX_LENGTH) || (shareNameEnd >= string.length() - 1)) {
            return null;
        }
        final int wildcardFlags = validateWildcards(string, shareNameEnd + 1);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(string, shareNameEnd, wildcardFlags);
    }

    /**
     * Validates and creates a Shared Topic Filter from the given Share Name and the given Topic Filter.
     *
     * @param shareName   the Share Name.
     * @param topicFilter the Topic Filter.
     * @return the created Shared Topic Filter or null if the Share Name is not a valid Share Name or the Topic Filter
     * is not a valid Topic Filter.
     */
    @Nullable
    public static Mqtt5SharedTopicFilter from(@NotNull final String shareName, @NotNull final String topicFilter) {
        if ((topicFilter.length() == 0) || !validateShareName(shareName)) {
            return null;
        }
        final int wildcardFlags = validateWildcards(topicFilter, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(shareName, topicFilter, wildcardFlags);
    }

    /**
     * Validates if the given UTF-16 encoded Java string is a valid Share Name.
     *
     * @param string the UTF-16 encoded Java string.
     * @return whether the string is a valid Share Name.
     */
    private static boolean validateShareName(@NotNull final String string) {
        if ((string.length() == 0) || containsMustNotCharacters(string)) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if ((c == MULTI_LEVEL_WILDCARD) || (c == SINGLE_LEVEL_WILDCARD) ||
                    (c == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR)) {
                return false;
            }
        }
        return true;
    }


    private int shareNameCharEnd;

    private Mqtt5SharedTopicFilter(@NotNull final byte[] binary, final int wildcardFlags) {
        super(binary, wildcardFlags);
        shareNameCharEnd = -1;
    }

    private Mqtt5SharedTopicFilter(@NotNull final String string, final int shareNameCharEnd, final int wildcardFlags) {
        super(string, wildcardFlags);
        this.shareNameCharEnd = shareNameCharEnd;
    }

    private Mqtt5SharedTopicFilter(
            @NotNull final String shareName, @NotNull final String topicFilter, final int wildcardFlags) {

        super(SHARE_PREFIX + shareName + Mqtt5Topic.TOPIC_LEVEL_SEPARATOR + topicFilter, wildcardFlags);
        this.shareNameCharEnd = SHARE_PREFIX_LENGTH + shareName.length();
    }

    @NotNull
    @Override
    public ImmutableList<String> getLevels() {
        return Mqtt5Topic.splitLevels(getTopicFilter());
    }

    @Override
    public boolean isShared() {
        return true;
    }

    /**
     * @return the Share Name of this Shared Topic Filter.
     */
    public String getShareName() {
        return toString().substring(SHARE_PREFIX_LENGTH, getShareNameCharEnd());
    }

    /**
     * @return the Topic Filter of this Shared Topic Filter as a string.
     */
    public String getTopicFilter() {
        return toString().substring(getShareNameCharEnd() + 1);
    }

    private int getShareNameCharEnd() {
        final String string = toString();
        if (shareNameCharEnd == -1) {
            for (int i = SHARE_PREFIX_LENGTH; i < string.length(); i++) {
                if (string.charAt(i) == Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
                    shareNameCharEnd = i;
                    break;
                }
            }
        }
        return shareNameCharEnd;
    }

}
