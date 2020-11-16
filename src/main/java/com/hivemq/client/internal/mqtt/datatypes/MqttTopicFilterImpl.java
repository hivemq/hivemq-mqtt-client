/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 * @see MqttTopicFilter
 * @see MqttUtf8StringImpl
 */
@Unmodifiable
public class MqttTopicFilterImpl extends MqttUtf8StringImpl implements MqttTopicFilter {

    static final int WILDCARD_CHECK_FAILURE = -1;
    private static final int WILDCARD_FLAG_MULTI_LEVEL = 0b01;
    private static final int WILDCARD_FLAG_SINGLE_LEVEL = 0b10;
    private static final int WILDCARD_CHECK_STATE_NOT_BEFORE = 0;
    private static final int WILDCARD_CHECK_STATE_BEFORE = 1;
    private static final int WILDCARD_CHECK_STATE_MULTI_LEVEL = 2;
    private static final int WILDCARD_CHECK_STATE_SINGLE_LEVEL = 3;

    /**
     * Validates and creates a Topic Filter of the given UTF-16 encoded Java string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Filter.
     * @throws IllegalArgumentException if the string is not a valid Topic Filter.
     */
    @Contract("null -> fail")
    public static @NotNull MqttTopicFilterImpl of(final @Nullable String string) {
        Checks.notEmpty(string, "Topic filter");
        checkLength(string, "Topic filter");
        checkWellFormed(string, "Topic filter");
        if (MqttSharedTopicFilterImpl.isShared(string)) {
            return MqttSharedTopicFilterImpl.ofInternal(string);
        }
        final int wildcardFlags = validateWildcards(string, 0);
        return new MqttTopicFilterImpl(string, wildcardFlags);
    }

    /**
     * Creates a Topic Filter of the given Topic Name.
     *
     * @param topic the Topic Name.
     * @return the created Topic Filter.
     */
    public static @NotNull MqttTopicFilterImpl of(final @NotNull MqttTopicImpl topic) {
        return new MqttTopicFilterImpl(topic.toString(), 0);
    }

    /**
     * Creates a Topic Filter of the given Shared Topic Filter.
     *
     * @param sharedTopicFilter the Shared Topic Filter.
     * @return the created Topic Filter.
     */
    public static @NotNull MqttTopicFilterImpl of(final @NotNull MqttSharedTopicFilterImpl sharedTopicFilter) {
        return new MqttTopicFilterImpl(sharedTopicFilter.getTopicFilterString(), sharedTopicFilter.wildcardFlags);
    }

    /**
     * Validates and creates a Topic Filter of the given byte array with UTF-8 encoded data.
     *
     * @param binary the byte array with the UTF-8 encoded data.
     * @return the created Topic Filter or <code>null</code> if the byte array does not represent a valid Topic Filter.
     */
    public static @Nullable MqttTopicFilterImpl of(final byte @NotNull [] binary) {
        if ((binary.length == 0) || !MqttBinaryData.isInRange(binary) || isWellFormed(binary)) {
            return null;
        }
        if (MqttSharedTopicFilterImpl.isShared(binary)) {
            return MqttSharedTopicFilterImpl.ofInternal(binary);
        }
        final int wildcardFlags = validateWildcards(binary, 0);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new MqttTopicFilterImpl(binary, wildcardFlags);
    }

    /**
     * Validates and decodes a Topic Filter from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Topic Filter or <code>null</code> if the byte buffer does not contain a valid Topic Filter.
     */
    public static @Nullable MqttTopicFilterImpl decode(final @NotNull ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : of(binary);
    }

    /**
     * Validates the wildcards in the given byte array with UTF-8 encoded data.
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @param start  the index in the byte array to start validation at.
     * @return a combination of {@link #WILDCARD_FLAG_MULTI_LEVEL} and {@link #WILDCARD_FLAG_SINGLE_LEVEL} indicating
     *         that a multi-level and/or single-level wildcards are present in the byte array or {@link
     *         #WILDCARD_CHECK_FAILURE} if the wildcard characters are misplaced.
     */
    static int validateWildcards(final byte @NotNull [] binary, final int start) {
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
     * @param start  the character index in the string to start validation at.
     * @return a combination of {@link #WILDCARD_FLAG_MULTI_LEVEL} and {@link #WILDCARD_FLAG_SINGLE_LEVEL} indicating
     *         that a multi-level and/or single-level wildcards are present in the string.
     * @throws IllegalArgumentException if the wildcard characters are misplaced.
     */
    static int validateWildcards(final @NotNull String string, final int start) {
        int wildcardFlags = 0;

        int state = WILDCARD_CHECK_STATE_BEFORE;

        for (int i = start; i < string.length(); i++) {
            final char c = string.charAt(i);
            switch (state) {
                case WILDCARD_CHECK_STATE_NOT_BEFORE:
                    if ((c == SINGLE_LEVEL_WILDCARD) || (c == MULTI_LEVEL_WILDCARD)) {
                        throw new IllegalArgumentException("Topic filter [" + string.substring(start) +
                                "] contains misplaced wildcard characters. Wildcard (" + c + ") at index " +
                                (i - start) + " must follow a topic level separator.");
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
                    throw new IllegalArgumentException("Topic filter [" + string.substring(start) +
                            "] contains misplaced wildcard characters. Multi level wildcard (" + MULTI_LEVEL_WILDCARD +
                            ") must be the last character.");
                case WILDCARD_CHECK_STATE_SINGLE_LEVEL:
                    if (c != MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                        throw new IllegalArgumentException("Topic filter [" + string.substring(start) +
                                "] contains misplaced wildcard characters. Single level wildcard (" +
                                SINGLE_LEVEL_WILDCARD + ") at index " + (i - start - 1) +
                                " must be followed by a topic level separator.");
                    }
                    state = WILDCARD_CHECK_STATE_BEFORE;
                    break;
            }
        }

        return wildcardFlags;
    }

    final int wildcardFlags;

    MqttTopicFilterImpl(final byte @NotNull [] binary, final int wildcardFlags) {
        super(binary);
        this.wildcardFlags = wildcardFlags;
    }

    MqttTopicFilterImpl(final @NotNull String string, final int wildcardFlags) {
        super(string);
        this.wildcardFlags = wildcardFlags;
    }

    @Override
    public @NotNull ImmutableList<String> getLevels() {
        return MqttTopicImpl.splitLevels(getTopicFilterString());
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

    @Override
    public @NotNull MqttSharedTopicFilterImpl share(final @Nullable String shareName) {
        return MqttSharedTopicFilterImpl.of(shareName, this);
    }

    int getFilterByteStart() {
        return 0;
    }

    public @NotNull String getTopicFilterString() {
        return toString();
    }

    public byte @Nullable [] getPrefix() {
        final int filterByteStart = getFilterByteStart();
        return (filterByteStart == 0) ? null : Arrays.copyOfRange(toBinary(), 0, filterByteStart - 1);
    }

    @Override
    public boolean matches(final @Nullable MqttTopic topic) {
        return matches(MqttChecks.topic(topic));
    }

    public boolean matches(final @NotNull MqttTopicImpl topic) {
        return matches(toBinary(), getFilterByteStart(), topic.toBinary());
    }

    private static boolean matches(final byte @NotNull [] filter, final int offset, final byte @NotNull [] topic) {
        int fi = offset;
        int ti = 0;
        while (fi < filter.length) {
            final byte fb = filter[fi++];
            if (fb == MULTI_LEVEL_WILDCARD) {
                return true;
            } else if (fb == SINGLE_LEVEL_WILDCARD) {
                while (ti < topic.length) { // loop until next topic level separator or end
                    if (topic[ti] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                        break;
                    }
                    ti++; // only increment when not topic level separator
                }
            } else {
                if (ti == topic.length) {
                    // lookahead for "/#" as it includes the parent level
                    return (fb == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) && (fi + 1 == filter.length) &&
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
    public boolean matches(final @Nullable MqttTopicFilter topicFilter) {
        return matches(MqttChecks.topicFilter(topicFilter));
    }

    public boolean matches(final @NotNull MqttTopicFilterImpl topicFilter) {
        return matches(toBinary(), getFilterByteStart(), topicFilter.toBinary(), topicFilter.getFilterByteStart());
    }

    private static boolean matches(
            final byte @NotNull [] filter1, final int offset1, final byte @NotNull [] filter2, final int offset2) {

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
                    if (filter2[i2] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                        break;
                    }
                    i2++; // only increment when not topic level separator
                }
            } else {
                if (i2 == filter2.length) {
                    // lookahead for "/#" as it includes the parent level
                    return (b1 == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) && (i1 + 1 == filter1.length) &&
                            (filter1[i1] == MULTI_LEVEL_WILDCARD);
                }
                if (filter2[i2++] != b1) {
                    return false;
                }
            }
        }
        return (i1 == filter1.length) && (i2 == filter2.length);
    }

    @Override
    public MqttTopicFilterImplBuilder.@NotNull Default extend() {
        return new MqttTopicFilterImplBuilder.Default(this);
    }
}
