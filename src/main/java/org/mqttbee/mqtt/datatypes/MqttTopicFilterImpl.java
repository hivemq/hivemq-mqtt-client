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

package org.mqttbee.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.util.MustNotBeImplementedUtil;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 * @see MqttTopicFilter
 * @see MqttUtf8StringImpl
 */
@Immutable
public class MqttTopicFilterImpl extends MqttUtf8StringImpl implements MqttTopicFilter {

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
    public static MqttTopicFilterImpl from(@NotNull final byte[] binary) {
        if ((binary.length == 0) || containsMustNotCharacters(binary)) {
            return null;
        }
        if (MqttSharedTopicFilterImpl.isShared(binary)) {
            return MqttSharedTopicFilterImpl.fromInternal(binary);
        }
        final int wildcardFlags = validateWildcards(binary, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new MqttTopicFilterImpl(binary, wildcardFlags);
    }

    /**
     * Validates and creates a Topic Filter from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Filter or null if the string is not a valid Topic Filter.
     */
    @Nullable
    public static MqttTopicFilterImpl from(@NotNull final String string) {
        if ((string.length() == 0) || containsMustNotCharacters(string)) {
            return null;
        }
        if (MqttSharedTopicFilterImpl.isShared(string)) {
            return MqttSharedTopicFilterImpl.fromInternal(string);
        }
        final int wildcardFlags = validateWildcards(string, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new MqttTopicFilterImpl(string, wildcardFlags);
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
    public static MqttTopicFilterImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
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
                    if (b == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                        case MqttTopicImpl.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (b != MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                    if (c == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
                        case MqttTopicImpl.TOPIC_LEVEL_SEPARATOR:
                            state = WILDCARD_CHECK_STATE_BEFORE;
                            break;
                        default:
                            state = WILDCARD_CHECK_STATE_NOT_BEFORE;
                    }
                    break;
                case WILDCARD_CHECK_STATE_MULTI_LEVEL:
                    return WILDCARD_CHECK_FAILURE;
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (c != MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                        return WILDCARD_CHECK_FAILURE;
                    }
                    state = WILDCARD_CHECK_STATE_BEFORE;
                    break;
            }
        }

        return wildcardFlags;
    }


    private final int wildcardFlags;

    MqttTopicFilterImpl(@NotNull final byte[] binary, final int wildcardFlags) {
        super(binary);
        this.wildcardFlags = wildcardFlags;
    }

    MqttTopicFilterImpl(@NotNull final String string, final int wildcardFlags) {
        super(string);
        this.wildcardFlags = wildcardFlags;
    }

    @NotNull
    @Override
    public ImmutableList<String> getLevels() {
        return MqttTopicImpl.splitLevels(toString());
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

    int getFilterByteStart() {
        return 0;
    }

    @Override
    public boolean matches(@NotNull final MqttTopic topic) {
        return matches(MustNotBeImplementedUtil.checkNotImplemented(topic, MqttTopicImpl.class));
    }

    public boolean matches(@NotNull final MqttTopicImpl topic) {
        return matches(toBinary(), getFilterByteStart(), topic.toBinary());
    }

    private static boolean matches(@NotNull final byte[] filter, final int offset, @NotNull final byte[] topic) {
        int fi = offset;
        int ti = 0;
        while (fi < filter.length) {
            final byte fb = filter[fi++];
            if (fb == MULTI_LEVEL_WILDCARD) {
                return true;
            } else if (fb == SINGLE_LEVEL_WILDCARD) {
                while (ti < topic.length) { // loop until next topic level separator or end
                    if (topic[ti] == MqttTopic.TOPIC_LEVEL_SEPARATOR) {
                        break;
                    }
                    ti++; // only increment when not topic level separator
                }
            } else {
                if (ti == topic.length) {
                    // lookahead for "/#" as it includes the parent level
                    return (fb == MqttTopic.TOPIC_LEVEL_SEPARATOR) && (fi + 1 == filter.length) &&
                            (filter[fi] == MULTI_LEVEL_WILDCARD);
                }
                if (topic[ti++] != fb) {
                    return false;
                }
            }
        }
        return (fi == filter.length) && (ti == topic.length);
    }

    @Override
    public boolean matches(@NotNull final MqttTopicFilter topicFilter) {
        return matches(MustNotBeImplementedUtil.checkNotImplemented(topicFilter, MqttTopicFilterImpl.class));
    }

    public boolean matches(@NotNull final MqttTopicFilterImpl topicFilter) {
        return matches(toBinary(), getFilterByteStart(), topicFilter.toBinary(), topicFilter.getFilterByteStart());
    }

    private static boolean matches(
            @NotNull final byte[] filter1, final int offset1, @NotNull final byte[] filter2, final int offset2) {

        int i1 = offset1;
        int i2 = offset2;
        while (i1 < filter1.length) {
            final byte b1 = filter1[i1++];
            if (b1 == MULTI_LEVEL_WILDCARD) {
                return true;
            } else if (b1 == SINGLE_LEVEL_WILDCARD) {
                if (filter2[i2] == MULTI_LEVEL_WILDCARD) { // single level wildcard is weaker
                    return false;
                }
                while (i2 < filter2.length) { // loop until next topic level separator or end
                    if (filter2[i2] == MqttTopic.TOPIC_LEVEL_SEPARATOR) {
                        break;
                    }
                    i2++; // only increment when not topic level separator
                }
            } else {
                if (i2 == filter2.length) {
                    // lookahead for "/#" as it includes the parent level
                    return (b1 == MqttTopic.TOPIC_LEVEL_SEPARATOR) && (i1 + 1 == filter1.length) &&
                            (filter1[i1] == MULTI_LEVEL_WILDCARD);
                }
                if (filter2[i2++] != b1) {
                    return false;
                }
            }
        }
        return (i1 == filter1.length) && (i2 == filter2.length);
    }

}
