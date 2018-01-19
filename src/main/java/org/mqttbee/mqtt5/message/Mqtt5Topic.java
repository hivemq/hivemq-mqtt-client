package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

/**
 * MQTT Topic Name according to the MQTT 5 specification.
 * <p>
 * This class lazily en/decodes between UTF-8 and UTF-16 encoding, but performs validation upfront.
 * <p>
 * A Topic Name has the restrictions from {@link Mqtt5UTF8String}, must be at least 1 character long and mut not contain
 * wildcards ({@link Mqtt5TopicFilter#MULTI_LEVEL_WILDCARD}, {@link Mqtt5TopicFilter#SINGLE_LEVEL_WILDCARD}).
 *
 * @author Silvio Giebl
 */
public class Mqtt5Topic extends Mqtt5UTF8String {

    /**
     * The topic level separator character.
     */
    public static final char TOPIC_LEVEL_SEPARATOR = '/';

    /**
     * Validates and decodes a Topic Name from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created Topic Name or null if the byte array does not contain a well-formed Topic Name.
     */
    @Nullable
    public static Mqtt5Topic from(@NotNull final byte[] binary) {
        return (binary.length == 0) || containsMustNotCharacters(binary) ? null : new Mqtt5Topic(binary);
    }

    /**
     * Validates and creates a Topic Name from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Name or null if the string is not a valid Topic Name.
     */
    @Nullable
    public static Mqtt5Topic from(@NotNull final String string) {
        return (string.length() == 0) || containsMustNotCharacters(string) ? null : new Mqtt5Topic(string);
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
    public static Mqtt5Topic from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = Mqtt5DataTypes.decodeBinaryData(byteBuf);
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
     * @see Mqtt5UTF8String#containsMustNotCharacters(byte[])
     * @see #containsWildcardCharacters(byte[])
     */
    static boolean containsMustNotCharacters(@NotNull final byte[] binary) {
        return Mqtt5UTF8String.containsMustNotCharacters(binary) || containsWildcardCharacters(binary);
    }

    /**
     * Checks whether the given UTF-16 encoded Java string contains characters a Topic Name must not contain according
     * to the MQTT 5 specification.
     * <p>
     * These characters are the characters a UTF-8 encoded String must not contain and wildcard characters.
     *
     * @param string the UTF-16 encoded Java string.
     * @return whether the string contains characters a Topic Name must not contain.
     * @see Mqtt5UTF8String#containsMustNotCharacters(String)
     * @see #containsWildcardCharacters(String)
     */
    static boolean containsMustNotCharacters(@NotNull final String string) {
        return Mqtt5UTF8String.containsMustNotCharacters(string) || containsWildcardCharacters(string);
    }

    /**
     * Checks whether the given UTF-8 encoded byte array contains wildcard characters.
     *
     * @param binary the UTF-8 encoded byte array.
     * @return whether the byte array contains wildcard characters.
     */
    private static boolean containsWildcardCharacters(@NotNull final byte[] binary) {
        for (final byte b : binary) {
            if (b == Mqtt5TopicFilter.MULTI_LEVEL_WILDCARD || b == Mqtt5TopicFilter.SINGLE_LEVEL_WILDCARD) {
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
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == Mqtt5TopicFilter.MULTI_LEVEL_WILDCARD || c == Mqtt5TopicFilter.SINGLE_LEVEL_WILDCARD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Splits the levels of the given Topic Name string.
     *
     * @param string the Topic Name string.
     * @return the levels of the Topic Name string.
     */
    @NotNull
    static ImmutableList<String> splitLevels(@NotNull final String string) {
        int startIndex = 0;
        final ImmutableList.Builder<String> levelsBuilder = ImmutableList.builder();
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == TOPIC_LEVEL_SEPARATOR) {
                levelsBuilder.add(string.substring(startIndex, i));
                startIndex = i + 1;
            }
        }
        levelsBuilder.add(string.substring(startIndex, string.length()));
        return levelsBuilder.build();
    }


    private Mqtt5Topic(@NotNull final byte[] binary) {
        super(binary);
    }

    private Mqtt5Topic(@NotNull final String string) {
        super(string);
    }

    /**
     * @return the levels of this Topic Name.
     */
    @NotNull
    public ImmutableList<String> getLevels() {
        return splitLevels(toString());
    }

}
