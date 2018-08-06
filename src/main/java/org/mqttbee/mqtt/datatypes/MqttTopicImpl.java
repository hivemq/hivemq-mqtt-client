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

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 * @see MqttTopic
 * @see MqttUtf8StringImpl
 */
@Immutable
public class MqttTopicImpl extends MqttUtf8StringImpl implements MqttTopic {

    /**
     * Validates and decodes a Topic Name from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created Topic Name or null if the byte array does not contain a well-formed Topic Name.
     */
    @Nullable
    public static MqttTopicImpl from(@NotNull final byte[] binary) {
        return (binary.length == 0) || !MqttBinaryData.isInRange(binary) || containsMustNotCharacters(binary) ? null :
                new MqttTopicImpl(binary);
    }

    /**
     * Validates and creates a Topic Name from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Name or null if the string is not a valid Topic Name.
     */
    @Nullable
    public static MqttTopicImpl from(@NotNull final String string) {
        return (string.length() == 0) || containsMustNotCharacters(string) ? null : new MqttTopicImpl(string);
    }

    /**
     * Validates and decodes a Topic Name from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Topic Name or null if the byte buffer does not contain a well-formed Topic Name.
     */
    @Nullable
    public static MqttTopicImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : from(binary);
    }

    /**
     * Checks whether the given UTF-8 encoded byte array contains characters a Topic Name must not contain according to
     * the MQTT 5 specification.
     * <p>
     * These characters are the characters a UTF-8 encoded String must not contain and wildcard characters.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return whether the byte array contains characters a Topic Name must not contain.
     * @see MqttUtf8StringImpl#containsMustNotCharacters(byte[])
     * @see #containsWildcardCharacters(byte[])
     */
    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
        return MqttUtf8StringImpl.containsMustNotCharacters(binary) || containsWildcardCharacters(binary);
    }

    /**
     * Checks whether the given UTF-16 encoded Java string contains characters a Topic Name must not contain according
     * to the MQTT 5 specification.
     * <p>
     * These characters are the characters a UTF-8 encoded String must not contain and wildcard characters.
     *
     * @param string the UTF-16 encoded Java string.
     * @return whether the string contains characters a Topic Name must not contain.
     * @throws IllegalArgumentException if the given string contains forbidden characters.
     * @see MqttUtf8StringImpl#checkForbiddenCharacters(String)
     * @see #containsWildcardCharacters(String)
     */
    static boolean containsMustNotCharacters(@NotNull final String string) {
        checkForbiddenCharacters(string);
        return containsWildcardCharacters(string);
    }

    /**
     * Checks whether the given UTF-8 encoded byte array contains wildcard characters.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return whether the byte array contains wildcard characters.
     */
    private static boolean containsWildcardCharacters(@NotNull final byte[] binary) {
        for (final byte b : binary) {
            if (b == MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD || b == MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given UTF-16 encoded Java string contains wildcard characters.
     *
     * @param string the UTF-16 encoded Java string.
     * @return whether the string contains wildcard characters.
     */
    private static boolean containsWildcardCharacters(@NotNull final String string) {
        return (string.indexOf(MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD) != -1) ||
                (string.indexOf(MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD) != -1);
    }

    /**
     * Splits the levels of the given Topic Name string.
     *
     * @param string the Topic Name string.
     * @return the levels of the Topic Name string.
     */
    @NotNull
    static ImmutableList<String> splitLevels(@NotNull final String string) {
        final ImmutableList.Builder<String> levelsBuilder = ImmutableList.builder();
        int start = 0;
        while (true) {
            final int end = string.indexOf(TOPIC_LEVEL_SEPARATOR, start);
            if (end == -1) {
                levelsBuilder.add(string.substring(start, string.length()));
                return levelsBuilder.build();
            }
            levelsBuilder.add(string.substring(start, end));
            start = end + 1;
        }
    }


    private MqttTopicImpl(@NotNull final byte[] binary) {
        super(binary);
    }

    private MqttTopicImpl(@NotNull final String string) {
        super(string);
    }

    @NotNull
    @Override
    public ImmutableList<String> getLevels() {
        return splitLevels(toString());
    }

}
