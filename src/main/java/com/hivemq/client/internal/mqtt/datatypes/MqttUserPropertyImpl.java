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

import com.hivemq.client.internal.mqtt.message.MqttProperty;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 * @see Mqtt5UserProperty
 */
@Unmodifiable
public class MqttUserPropertyImpl implements Mqtt5UserProperty {

    /**
     * Creates an User Property of the given name and value.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the created User Property.
     */
    @Contract("null, _ -> fail; _, null -> fail")
    public static @NotNull MqttUserPropertyImpl of(final @Nullable String name, final @Nullable String value) {
        return of(
                MqttUtf8StringImpl.of(name, "User property name"), MqttUtf8StringImpl.of(value, "User property value"));
    }

    /**
     * Creates an User Property of the given name and value.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the created User Property.
     */
    public static @NotNull MqttUserPropertyImpl of(
            final @NotNull MqttUtf8StringImpl name, final @NotNull MqttUtf8StringImpl value) {

        return new MqttUserPropertyImpl(name, value);
    }

    /**
     * Validates and decodes a User Property from the given byte buffer at the current reader index.
     *
     * @param in the byte buffer to decode from.
     * @return the decoded User Property or null if the name and/or value are not valid UTF-8 encoded Strings.
     */
    public static @Nullable MqttUserPropertyImpl decode(final @NotNull ByteBuf in) {
        final MqttUtf8StringImpl name = MqttUtf8StringImpl.decode(in);
        if (name == null) {
            return null;
        }
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.decode(in);
        if (value == null) {
            return null;
        }
        return new MqttUserPropertyImpl(name, value);
    }

    private final @NotNull MqttUtf8StringImpl name;
    private final @NotNull MqttUtf8StringImpl value;

    public MqttUserPropertyImpl(final @NotNull MqttUtf8StringImpl name, final @NotNull MqttUtf8StringImpl value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public @NotNull MqttUtf8StringImpl getName() {
        return name;
    }

    @Override
    public @NotNull MqttUtf8StringImpl getValue() {
        return value;
    }

    void encode(final @NotNull ByteBuf out) {
        out.writeByte(MqttProperty.USER_PROPERTY);
        name.encode(out);
        value.encode(out);
    }

    int encodedLength() {
        return 1 + name.encodedLength() + value.encodedLength();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttUserPropertyImpl)) {
            return false;
        }
        final MqttUserPropertyImpl that = (MqttUserPropertyImpl) o;
        return name.equals(that.name) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    @Override
    public int compareTo(final @NotNull Mqtt5UserProperty that) {
        final int nameComparison = name.compareTo(that.getName());
        return (nameComparison != 0) ? nameComparison : value.compareTo(that.getValue());
    }

    @Override
    public @NotNull String toString() {
        return "(" + name + ", " + value + ")";
    }
}
