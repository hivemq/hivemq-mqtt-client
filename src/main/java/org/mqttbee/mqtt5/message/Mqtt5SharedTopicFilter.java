package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SharedTopicFilter extends Mqtt5TopicFilter {

    private static final String SHARE_PREFIX = "$share" + Mqtt5Topic.TOPIC_LEVEL_SEPARATOR;
    private static final int SHARE_PREFIX_LENGTH = SHARE_PREFIX.length();

    static boolean isShared(@NotNull final byte[] binary) {
        for (int i = 0; i < SHARE_PREFIX_LENGTH; i++) {
            if (binary[i] != SHARE_PREFIX.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    static boolean isShared(@NotNull final String string) {
        return string.startsWith(SHARE_PREFIX);
    }

    @Nullable
    static Mqtt5SharedTopicFilter fromInternal(@NotNull final byte[] binary) {
        // no isShared and containsMustNotCharacters, already checked in TopicFilter
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        byte b;
        while ((b = binary[shareNameEnd]) != Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
            if (b == MULTI_LEVEL_WILDCARD || b == SINGLE_LEVEL_WILDCARD) {
                return null;
            }
            shareNameEnd++;
        }
        final int wildcardFlags = checkWildcards(binary, shareNameEnd);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(binary, wildcardFlags);
    }

    @Nullable
    static Mqtt5SharedTopicFilter fromInternal(@NotNull final String string) {
        // no isShared and containsMustNotCharacters, already checked in TopicFilter
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        char c;
        while ((c = string.charAt(shareNameEnd)) != Mqtt5Topic.TOPIC_LEVEL_SEPARATOR) {
            if (c == MULTI_LEVEL_WILDCARD || c == SINGLE_LEVEL_WILDCARD) {
                return null;
            }
            shareNameEnd++;
        }
        final int wildcardFlags = checkWildcards(string, shareNameEnd);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(string, shareNameEnd, wildcardFlags);
    }

    @Nullable
    public static Mqtt5SharedTopicFilter from(@NotNull final String shareName, @NotNull final String topicFilter) {
        if (containsMustNotCharacters(shareName) || Mqtt5Topic.containsWildcardCharacters(shareName)) {
            return null;
        }
        final int wildcardFlags = checkWildcards(topicFilter, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(shareName, topicFilter, wildcardFlags);
    }


    private int shareNameCharEnd;

    private Mqtt5SharedTopicFilter(@NotNull final byte[] binary, final int wildcardFlags) {
        super(binary, wildcardFlags);
    }

    private Mqtt5SharedTopicFilter(@NotNull final String string, final int shareNameCharEnd, final int wildcardFlags) {
        super(string, wildcardFlags);
        this.shareNameCharEnd = shareNameCharEnd;
    }

    public Mqtt5SharedTopicFilter(
            @NotNull final String shareName, @NotNull final String topicFilter, final int wildcardFlags) {

        super(SHARE_PREFIX + shareName + Mqtt5Topic.TOPIC_LEVEL_SEPARATOR + topicFilter, wildcardFlags);
        this.shareNameCharEnd = SHARE_PREFIX_LENGTH + shareName.length();
    }

    public String getShareName() {
        return toString().substring(SHARE_PREFIX_LENGTH, shareNameCharEnd);
    }

    public String getTopicFilter() {
        return toString().substring(shareNameCharEnd);
    }

    @NotNull
    @Override
    public String[] getLevels() {
        return getTopicFilter().split(Character.toString((char) Mqtt5Topic.TOPIC_LEVEL_SEPARATOR));
    }

    @Override
    public boolean isShared() {
        return true;
    }

}
