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

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 * @see MqttTopic
 * @see MqttUtf8StringImpl
 */
@Unmodifiable
public class MqttTopicImpl extends MqttUtf8StringImpl implements MqttTopic {

    /**
     * Validates and creates a Topic Name of the given UTF-16 encoded Java string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Name.
     * @throws IllegalArgumentException if the given string is not a valid Topic Name.
     */
    @Contract("null -> fail")
    public static @NotNull MqttTopicImpl of(final @Nullable String string) {
        return of(string, "Topic");
    }

    /**
     * Same function as {@link #of(String)}, but allows specifying a name to use in error messages.
     *
     * @param string see {@link #of(String)}.
     * @param name   specific name used in error messages.
     * @return see {@link #of(String)}.
     * @see #of(String)
     */
    @Contract("null, _ -> fail")
    public static @NotNull MqttTopicImpl of(final @Nullable String string, final @NotNull String name) {
        Checks.notEmpty(string, name);
        checkLength(string, name);
        checkWellFormed(string, name);
        return new MqttTopicImpl(string);
    }

    /**
     * Validates and creates a Topic Name of the given byte array with UTF-8 encoded data.
     *
     * @param binary the byte array with the UTF-8 encoded data.
     * @return the created Topic Name or <code>null</code> if the byte array does not represent a valid Topic Name.
     */
    public static @Nullable MqttTopicImpl of(final byte @NotNull [] binary) {
        return (binary.length == 0) || !MqttBinaryData.isInRange(binary) || isWellFormed(binary) ? null :
                new MqttTopicImpl(binary);
    }

    /**
     * Validates and decodes a Topic Name from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Topic Name or <code>null</code> if the byte buffer does not contain a valid Topic Name.
     */
    public static @Nullable MqttTopicImpl decode(final @NotNull ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : of(binary);
    }

    /**
     * Checks if the given byte array with UTF-8 encoded data represents a well-formed Topic Name according to the MQTT
     * specification.
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @return whether the byte array represents a well-formed Topic Name.
     * @see MqttUtf8StringImpl#isWellFormed(byte[])
     * @see #containsWildcardCharacters(byte[])
     */
    static boolean isWellFormed(final byte @NotNull [] binary) {
        return MqttUtf8StringImpl.isWellFormed(binary) || containsWildcardCharacters(binary);
    }

    /**
     * Checks if the given UTF-16 encoded Java string is a well-formed Topic Name according to the MQTT specification.
     *
     * @param string the UTF-16 encoded Java string.
     * @param name   specific name used in error messages.
     * @throws IllegalArgumentException if the string is not a well-formed Topic Name.
     * @see MqttUtf8StringImpl#checkWellFormed(String, String)
     * @see #checkNoWildcardCharacters(String, String)
     */
    static void checkWellFormed(final @NotNull String string, final @NotNull String name) {
        MqttUtf8StringImpl.checkWellFormed(string, name);
        checkNoWildcardCharacters(string, name);
    }

    /**
     * Checks if the given byte array with UTF-8 encoded data contains wildcard characters.
     *
     * @param binary the byte array with UTF-8 encoded data.
     * @return whether the byte array contains wildcard characters.
     */
    private static boolean containsWildcardCharacters(final byte @NotNull [] binary) {
        for (final byte b : binary) {
            if (b == MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD || b == MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given UTF-16 encoded Java string does not contain wildcard characters.
     *
     * @param string the UTF-16 encoded Java string.
     * @param name   specific name used in error messages.
     * @throws IllegalArgumentException if the given string contains wildcard characters.
     */
    private static void checkNoWildcardCharacters(final @NotNull String string, final @NotNull String name) {
        final int multiLevelIndex = string.indexOf(MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD);
        if (multiLevelIndex != -1) {
            throw new IllegalArgumentException(name + " [" + string + "] must not contain multi level wildcard (" +
                    MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD + "), found at index " + multiLevelIndex + ".");
        }
        final int singleLevelIndex = string.indexOf(MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD);
        if (singleLevelIndex != -1) {
            throw new IllegalArgumentException(name + " [" + string + "] must not contain single level wildcard (" +
                    MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD + "), found at index " + singleLevelIndex + ".");
        }
    }

    /**
     * Splits the levels of the given Topic Name string.
     *
     * @param string the Topic Name string.
     * @return the levels of the Topic Name string.
     */
    static @NotNull ImmutableList<String> splitLevels(final @NotNull String string) {
        final ImmutableList.Builder<String> levelsBuilder = ImmutableList.builder();
        int start = 0;
        while (true) {
            final int end = string.indexOf(TOPIC_LEVEL_SEPARATOR, start);
            if (end == -1) {
                levelsBuilder.add(string.substring(start));
                return levelsBuilder.build();
            }
            levelsBuilder.add(string.substring(start, end));
            start = end + 1;
        }
    }

    private MqttTopicImpl(final byte @NotNull [] binary) {
        super(binary);
    }

    private MqttTopicImpl(final @NotNull String string) {
        super(string);
    }

    @Override
    public @NotNull ImmutableList<String> getLevels() {
        return splitLevels(toString());
    }

    @Override
    public @NotNull MqttTopicFilterImpl filter() {
        return MqttTopicFilterImpl.of(this);
    }

    @Override
    public MqttTopicImplBuilder.@NotNull Default extend() {
        return new MqttTopicImplBuilder.Default(this);
    }
}
