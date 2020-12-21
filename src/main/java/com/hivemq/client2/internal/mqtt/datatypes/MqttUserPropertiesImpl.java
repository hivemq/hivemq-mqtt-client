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

import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 * @see Mqtt5UserProperties
 */
@Unmodifiable
public class MqttUserPropertiesImpl implements Mqtt5UserProperties {

    /**
     * Empty collection of User Properties.
     */
    public static final @NotNull MqttUserPropertiesImpl NO_USER_PROPERTIES =
            new MqttUserPropertiesImpl(ImmutableList.of());

    /**
     * Creates a collection of User Properties from the given immutable list of User Properties.
     *
     * @param userProperties the immutable list of User Properties.
     * @return the created collection of User Properties or {@link #NO_USER_PROPERTIES} if the list is empty.
     */
    public static @NotNull MqttUserPropertiesImpl of(
            final @NotNull ImmutableList<MqttUserPropertyImpl> userProperties) {

        return userProperties.isEmpty() ? NO_USER_PROPERTIES : new MqttUserPropertiesImpl(userProperties);
    }

    /**
     * Builds a collection of User Properties from the given builder.
     *
     * @param userPropertiesBuilder the builder for the User Properties.
     * @return the built collection of User Properties or {@link #NO_USER_PROPERTIES} if the builder is null.
     */
    public static @NotNull MqttUserPropertiesImpl build(
            final @Nullable ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder) {

        return (userPropertiesBuilder == null) ? NO_USER_PROPERTIES : of(userPropertiesBuilder.build());
    }

    private final @NotNull ImmutableList<MqttUserPropertyImpl> userProperties;
    private int encodedLength = -1;

    private MqttUserPropertiesImpl(final @NotNull ImmutableList<MqttUserPropertyImpl> userProperties) {
        this.userProperties = userProperties;
    }

    @Override
    public @NotNull ImmutableList<MqttUserPropertyImpl> asList() {
        return userProperties;
    }

    /**
     * Encodes this collection of User Properties to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if name and value can not be encoded due to byte count restrictions. This check is
     * performed with the method {@link #encodedLength()} which is generally called before this method.
     *
     * @param out the byte buffer to encode to.
     */
    public void encode(final @NotNull ByteBuf out) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < userProperties.size(); i++) {
            userProperties.get(i).encode(out);
        }
    }

    /**
     * Calculates the byte count of this collection of User Properties according to the MQTT 5 specification.
     *
     * @return the encoded length of this collection of User Properties.
     */
    public int encodedLength() {
        if (encodedLength == -1) {
            encodedLength = calculateEncodedLength();
        }
        return encodedLength;
    }

    private int calculateEncodedLength() {
        int encodedLength = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < userProperties.size(); i++) {
            encodedLength += userProperties.get(i).encodedLength();
        }
        return encodedLength;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttUserPropertiesImpl)) {
            return false;
        }
        final MqttUserPropertiesImpl that = (MqttUserPropertiesImpl) o;
        return userProperties.equals(that.userProperties);
    }

    @Override
    public int hashCode() {
        return userProperties.hashCode();
    }

    @Override
    public MqttUserPropertiesImplBuilder.@NotNull Default extend() {
        return new MqttUserPropertiesImplBuilder.Default(this);
    }

    @Override
    public @NotNull String toString() {
        return userProperties.toString();
    }
}
