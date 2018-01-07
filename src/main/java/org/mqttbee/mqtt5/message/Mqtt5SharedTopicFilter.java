package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SharedTopicFilter extends Mqtt5TopicFilter {

    private static final String SHARE_PREFIX = "$share" + Mqtt5Topic.TOPIC_LEVEL_SEPARATOR;
    private static final int SHARE_PREFIX_LENGTH = SHARE_PREFIX.length();

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

    static boolean isShared(@NotNull final String string) {
        return string.startsWith(SHARE_PREFIX);
    }

    @Nullable
    static Mqtt5SharedTopicFilter fromInternal(@NotNull final byte[] binary) {
        // no isShared and containsMustNotCharacters, already checked in TopicFilter
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
        final int wildcardFlags = checkWildcards(binary, shareNameEnd + 1);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(binary, wildcardFlags);
    }

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
        final int wildcardFlags = checkWildcards(string, shareNameEnd + 1);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new Mqtt5SharedTopicFilter(string, shareNameEnd, wildcardFlags);
    }

    @Nullable
    public static Mqtt5SharedTopicFilter from(@NotNull final String shareName, @NotNull final String topicFilter) {
        if (containsMustNotCharacters(shareName) || Mqtt5Topic.containsWildcardCharacters(shareName) ||
                (shareName.length() == 0) || (topicFilter.length() == 0)) {
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

    public String getShareName() {
        return toString().substring(SHARE_PREFIX_LENGTH, getShareNameCharEnd());
    }

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
