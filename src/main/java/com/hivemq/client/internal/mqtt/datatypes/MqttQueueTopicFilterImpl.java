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

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttQueueTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilterBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 * @see MqttQueueTopicFilter
 * @see MqttUtf8StringImpl
 */
@Immutable
public class MqttQueueTopicFilterImpl extends MqttTopicFilterImpl implements MqttQueueTopicFilter {

    private static final int QUEUE_PREFIX_LENGTH = QUEUE_PREFIX.length();

    /**
     * Checks if the given UTF-16 encoded Java string represents a Queue Topic Filter. This method does not validate
     * whether it represents a valid Queue Topic Filter but only whether it starts with {@value #QUEUE_PREFIX}.
     *
     * @param string the given UTF-16 encoded Java string.
     * @return whether the string represents a Queue Topic Filter.
     */
    static boolean isQueue(final @NotNull String string) {
        return string.startsWith(QUEUE_PREFIX);
    }

    /**
     * Checks if the given byte array with UTF-8 encoded data represents a Queue Topic Filter. This method does not
     * validate whether it represents a valid Queue Topic Filter but only whether it starts with {@value
     * #QUEUE_PREFIX}.
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @return whether the byte array represents a Queue Topic Filter.
     */
    static boolean isQueue(final byte @NotNull [] binary) {
        if (binary.length < QUEUE_PREFIX_LENGTH) {
            return false;
        }
        for (int i = 0; i < QUEUE_PREFIX_LENGTH; i++) {
            if (binary[i] != QUEUE_PREFIX.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates and creates a Queue Topic Filter of the given UTF-16 encoded Java string.
     * <p>
     * This method does not validate {@link MqttUtf8StringImpl#checkLength(String, String) length}, {@link
     * MqttUtf8StringImpl#checkWellFormed(String, String) if well-formed} and {@link #isQueue(String) if queue}.
     *
     * @param string the UTF-16 encoded Java string starting with {@value #QUEUE_PREFIX}.
     * @return the created Queue Topic Filter.
     * @throws IllegalArgumentException if the string is not a valid Queue Topic Filter.
     */
    static @NotNull MqttQueueTopicFilterImpl ofInternal(final @NotNull String string) {
        if (string.length() <= QUEUE_PREFIX_LENGTH) {
            throw new IllegalArgumentException("Topic filter must be at least one character long.");
        }
        final int wildcardFlags = validateWildcards(string, QUEUE_PREFIX_LENGTH);
        return new MqttQueueTopicFilterImpl(string, wildcardFlags);
    }

    /**
     * Validates and creates a Queue Topic Filter of the given byte array with UTF-8 encoded data.
     * <p>
     * This method does not validate length, {@link MqttUtf8StringImpl#isWellFormed(byte[]) if well-formed} and {@link
     * #isQueue(byte[]) if queue}.
     *
     * @param binary the byte array with UTF-8 encoded data starting with {@value #QUEUE_PREFIX}.
     * @return the created Queue Topic Filter or <code>null</code> if the byte array is not a valid Queue Topic
     *         Filter.
     */
    static @Nullable MqttQueueTopicFilterImpl ofInternal(final byte @NotNull [] binary) {
        if (binary.length <= QUEUE_PREFIX_LENGTH) {
            return null;
        }
        final int wildcardFlags = validateWildcards(binary, QUEUE_PREFIX_LENGTH);
        if (wildcardFlags == WILDCARD_CHECK_FAILURE) {
            return null;
        }
        return new MqttQueueTopicFilterImpl(binary, wildcardFlags);
    }

    /**
     * Validates and creates a Queue Topic Filter of the given Topic Filter.
     *
     * @param topicFilter the Topic Filter string.
     * @return the created Queue Topic Filter.
     * @throws IllegalArgumentException if the Topic Filter string is not a valid Topic Filter.
     */
    @Contract("null -> fail")
    public static @NotNull MqttQueueTopicFilterImpl of(final @Nullable String topicFilter) {
        Checks.notEmpty(topicFilter, "Topic filter");
        checkWellFormed(topicFilter, "Topic filter");
        final String queueTopicFilter = QUEUE_PREFIX + topicFilter;
        checkLength(queueTopicFilter, "Queue topic filter");
        final int wildcardFlags = validateWildcards(topicFilter, 0);
        return new MqttQueueTopicFilterImpl(queueTopicFilter, wildcardFlags);
    }

    /**
     * Validates and creates a Queue Topic Filter of the given Topic Filter.
     *
     * @param topicFilter the Topic Filter.
     * @return the created Queue Topic Filter.
     */
    public static @NotNull MqttQueueTopicFilterImpl of(final @NotNull MqttTopicFilterImpl topicFilter) {
        final String queueTopicFilter = QUEUE_PREFIX + topicFilter;
        checkLength(queueTopicFilter, "Queue topic filter");
        return new MqttQueueTopicFilterImpl(queueTopicFilter, topicFilter.wildcardFlags);
    }

    private final int filterByteStart;
    private final int filterCharStart;

    private MqttQueueTopicFilterImpl(final byte @NotNull [] binary, final int wildcardFlags) {
        super(binary, wildcardFlags);
        this.filterByteStart = QUEUE_PREFIX_LENGTH;
        this.filterCharStart = -1;
    }

    private MqttQueueTopicFilterImpl(final @NotNull String string, final int wildcardFlags) {
        super(string, wildcardFlags);
        this.filterByteStart = -1;
        this.filterCharStart = QUEUE_PREFIX_LENGTH;
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
        return filterByteStart;
    }

    private int getFilterCharStart() {
        if (filterCharStart == -1) {
            return QUEUE_PREFIX_LENGTH;
        }
        return filterCharStart;
    }

    @Override
    public MqttTopicFilterBuilder.@NotNull Complete extendQueue() {
        return new MqttTopicFilterImplBuilder.Default(this);
    }
}
