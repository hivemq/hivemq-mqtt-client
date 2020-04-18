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

package com.hivemq.client.mqtt.mqtt5.message.publish.pubrel;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for a {@link Mqtt5PubRel}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5PubRelBuilder {

    /**
     * Set the optional {@link Mqtt5PubRel#getReasonString() Reason String}.
     *
     * @param reasonString the Reason String or <code>null</code> to remove any previously set Reason String.
     * @return the builder.
     */
    @NotNull Mqtt5PubRelBuilder reasonString(@Nullable String reasonString);

    /**
     * Set the optional {@link Mqtt5PubRel#getReasonString() Reason String}.
     *
     * @param reasonString the Reason String or <code>null</code> to remove any previously set Reason String.
     * @return the builder.
     */
    @NotNull Mqtt5PubRelBuilder reasonString(@Nullable MqttUtf8String reasonString);

    /**
     * Sets the {@link Mqtt5PubRel#getUserProperties() User Properties}.
     *
     * @param userProperties the User Properties.
     * @return the builder.
     */
    @NotNull Mqtt5PubRelBuilder userProperties(@NotNull Mqtt5UserProperties userProperties);

    /**
     * Fluent counterpart of {@link #userProperties(Mqtt5UserProperties)}.
     * <p>
     * Calling {@link Mqtt5UserPropertiesBuilder.Nested#applyUserProperties()} on the returned builder has the effect of
     * {@link Mqtt5UserProperties#extend() extending} the current User Properties.
     *
     * @return the fluent builder for the User Properties.
     * @see #userProperties(Mqtt5UserProperties)
     */
    @CheckReturnValue
    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends Mqtt5PubRelBuilder> userProperties();

    /**
     * @return the Reason Code of the PubRel message.
     */
    @NotNull Mqtt5PubRelReasonCode getReasonCode();
}
