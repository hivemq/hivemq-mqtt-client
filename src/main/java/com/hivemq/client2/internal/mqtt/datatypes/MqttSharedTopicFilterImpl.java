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

package com.hivemq.client2.internal.mqtt.datatypes;

import com.hivemq.client2.internal.util.ByteArrayUtil;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.datatypes.MqttSharedTopicFilter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 * @see MqttSharedTopicFilter
 * @see MqttUtf8StringImpl
 */
@Unmodifiable
public class MqttSharedTopicFilterImpl extends MqttTopicFilterImpl implements MqttSharedTopicFilter {

    private static final int SHARE_PREFIX_LENGTH = SHARE_PREFIX.length();

    /**
     * Checks if the given UTF-16 encoded Java string represents a Shared Topic Filter. This method does not validate
     * whether it represents a valid Shared Topic Filter but only whether it starts with {@value #SHARE_PREFIX}.
     *
     * @param string the given UTF-16 encoded Java string.
     * @return whether the string represents a Shared Topic Filter.
     */
    static boolean isShared(final @NotNull String string) {
        return string.startsWith(SHARE_PREFIX);
    }

    /**
     * Checks if the given byte array with UTF-8 encoded data represents a Shared Topic Filter. This method does not
     * validate whether it represents a valid Shared Topic Filter but only whether it starts with {@value
     * #SHARE_PREFIX}.
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @return whether the byte array represents a Shared Topic Filter.
     */
    static boolean isShared(final byte @NotNull [] binary) {
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
     * Validates and creates a Shared Topic Filter of the given UTF-16 encoded Java string.
     * <p>
     * This method does not validate {@link MqttUtf8StringImpl#checkLength(String, String) length}, {@link
     * MqttUtf8StringImpl#checkWellFormed(String, String) if well-fromed} and {@link #isShared(String) if shared}.
     *
     * @param string the UTF-16 encoded Java string staring with {@value #SHARE_PREFIX}.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the string is not a valid Shared Topic Filter.
     */
    static @NotNull MqttSharedTopicFilterImpl ofInternal(final @NotNull String string) {
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        while (shareNameEnd < string.length()) {
            final char c = string.charAt(shareNameEnd);
            if (c == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                break;
            }
            if (c == MULTI_LEVEL_WILDCARD) {
                throw new IllegalArgumentException(shareNameNoMultiLevelWildcard(getShareName(string), shareNameEnd));
            } else if (c == SINGLE_LEVEL_WILDCARD) {
                throw new IllegalArgumentException(shareNameNoSingleLevelWildcard(getShareName(string), shareNameEnd));
            }
            shareNameEnd++;
        }
        if (shareNameEnd == SHARE_PREFIX_LENGTH) {
            throw new IllegalArgumentException("Share name must be at least one character long.");
        } else if (shareNameEnd >= string.length() - 1) {
            throw new IllegalArgumentException("Topic filter must be at least one character long.");
        }
        final int wildcardFlags = validateWildcards(string, shareNameEnd + 1);
        return new MqttSharedTopicFilterImpl(string, shareNameEnd, wildcardFlags);
    }

    /**
     * Validates and creates a Shared Topic Filter of the given byte array with UTF-8 encoded data.
     * <p>
     * This method does not validate length, {@link MqttUtf8StringImpl#isWellFormed(byte[]) if well-formed} and {@link
     * #isShared(byte[]) if shared}.
     *
     * @param binary the byte array with UTF-8 encoded data staring with {@value #SHARE_PREFIX}.
     * @return the created Shared Topic Filter or <code>null</code> if the byte array is not a valid Shared Topic
     *         Filter.
     */
    static @Nullable MqttSharedTopicFilterImpl ofInternal(final byte @NotNull [] binary) {
        int shareNameEnd = SHARE_PREFIX_LENGTH;
        while (shareNameEnd < binary.length) {
            final byte b = binary[shareNameEnd];
            if (b == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
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
        return new MqttSharedTopicFilterImpl(binary, shareNameEnd, wildcardFlags);
    }

    /**
     * Validates and creates a Shared Topic Filter of the given Share Name and Topic Filter.
     *
     * @param shareName   the Share Name string.
     * @param topicFilter the Topic Filter string.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the Share Name string is not a valid Share Name or the Topic Filter string is
     *                                  not a valid Topic Filter.
     */
    @Contract("null, _ -> fail; _, null -> fail")
    public static @NotNull MqttSharedTopicFilterImpl of(
            final @Nullable String shareName, final @Nullable String topicFilter) {

        checkShareName(shareName);
        Checks.notEmpty(topicFilter, "Topic filter");
        checkWellFormed(topicFilter, "Topic filter");
        final String sharedTopicFilter = sharedTopicFilter(shareName, topicFilter);
        checkLength(sharedTopicFilter, "Shared topic filter");
        final int wildcardFlags = validateWildcards(topicFilter, 0);
        return new MqttSharedTopicFilterImpl(sharedTopicFilter, shareNameCharEnd(shareName), wildcardFlags);
    }

    /**
     * Validates and creates a Shared Topic Filter of the given Share Name and Topic Filter.
     *
     * @param shareName   the Share Name string.
     * @param topicFilter the Topic Filter.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the Share Name string is not a valid Share Name.
     */
    @Contract("null, _ -> fail")
    public static @NotNull MqttSharedTopicFilterImpl of(
            final @Nullable String shareName, final @NotNull MqttTopicFilterImpl topicFilter) {

        checkShareName(shareName);
        final String sharedTopicFilter = sharedTopicFilter(shareName, topicFilter.getTopicFilterString());
        checkLength(sharedTopicFilter, "Shared topic filter");
        return new MqttSharedTopicFilterImpl(sharedTopicFilter, shareNameCharEnd(shareName), topicFilter.wildcardFlags);
    }

    /**
     * Checks if the given UTF-16 encoded Java string is a well-formed share name.
     *
     * @param shareName the UTF-16 encoded Java string.
     * @throws IllegalArgumentException if the string is a well-formed share name.
     */
    @Contract("null -> fail")
    private static void checkShareName(final @Nullable String shareName) {
        Checks.notEmpty(shareName, "Share name");
        checkWellFormed(shareName, "Share name");
        for (int i = 0; i < shareName.length(); i++) {
            final char c = shareName.charAt(i);
            if (c == MULTI_LEVEL_WILDCARD) {
                throw new IllegalArgumentException(shareNameNoMultiLevelWildcard(shareName, i));
            } else if (c == SINGLE_LEVEL_WILDCARD) {
                throw new IllegalArgumentException(shareNameNoSingleLevelWildcard(shareName, i));
            } else if (c == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                throw new IllegalArgumentException(
                        "Share name [" + shareName + "] must not contain topic level separator (" +
                                MqttTopicImpl.TOPIC_LEVEL_SEPARATOR + "), found at index: " + i + ".");
            }
        }
    }

    private static @NotNull String getShareName(final @NotNull String sharedTopicFilter) {
        final int shareNameEnd = sharedTopicFilter.indexOf(MqttTopicImpl.TOPIC_LEVEL_SEPARATOR, SHARE_PREFIX_LENGTH);
        return sharedTopicFilter.substring(
                SHARE_PREFIX_LENGTH, (shareNameEnd == -1) ? sharedTopicFilter.length() : shareNameEnd);
    }

    private static @NotNull String shareNameNoMultiLevelWildcard(final @NotNull String shareName, final int index) {
        return "Share name [" + shareName + "] must not contain multi level wildcard (" + MULTI_LEVEL_WILDCARD +
                "), found at index " + index + ".";
    }

    private static @NotNull String shareNameNoSingleLevelWildcard(final @NotNull String shareName, final int index) {
        return "Share name [" + shareName + "] must not contain single level wildcard (" + SINGLE_LEVEL_WILDCARD +
                "), found at index " + index + ".";
    }

    private static @NotNull String sharedTopicFilter(
            final @NotNull String shareName, final @NotNull String topicFilter) {

        return SHARE_PREFIX + shareName + MqttTopicImpl.TOPIC_LEVEL_SEPARATOR + topicFilter;
    }

    private static int shareNameCharEnd(final @NotNull String shareName) {
        return SHARE_PREFIX_LENGTH + shareName.length();
    }

    private int filterByteStart;
    private int filterCharStart;

    private MqttSharedTopicFilterImpl(
            final byte @NotNull [] binary, final int shareNameByteEnd, final int wildcardFlags) {

        super(binary, wildcardFlags);
        this.filterByteStart = shareNameByteEnd + 1;
        filterCharStart = -1;
    }

    private MqttSharedTopicFilterImpl(
            final @NotNull String string, final int shareNameCharEnd, final int wildcardFlags) {

        super(string, wildcardFlags);
        filterByteStart = -1;
        this.filterCharStart = shareNameCharEnd + 1;
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public @NotNull String getShareName() {
        return toString().substring(SHARE_PREFIX_LENGTH, getFilterCharStart() - 1);
    }

    @Override
    public @NotNull String getTopicFilterString() {
        return toString().substring(getFilterCharStart());
    }

    @Override
    public @NotNull MqttTopicFilterImpl getTopicFilter() {
        return MqttTopicFilterImpl.of(this);
    }

    @Override
    int getFilterByteStart() {
        if (filterByteStart == -1) {
            filterByteStart = ByteArrayUtil.indexOf(toBinary(), SHARE_PREFIX_LENGTH + 1,
                    (byte) MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) + 1;
        }
        return filterByteStart;
    }

    private int getFilterCharStart() {
        if (filterCharStart == -1) {
            filterCharStart = toString().indexOf(MqttTopicImpl.TOPIC_LEVEL_SEPARATOR, SHARE_PREFIX_LENGTH + 1) + 1;
        }
        return filterCharStart;
    }

    @Override
    public MqttTopicFilterImplBuilder.@NotNull SharedDefault extendShared() {
        return new MqttTopicFilterImplBuilder.SharedDefault(this);
    }
}
