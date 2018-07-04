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

package org.mqttbee.mqtt.message.unsubscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;

/** @author Silvio Giebl */
@Immutable
public class Mqtt3UnsubscribeView implements Mqtt3Unsubscribe {

    @NotNull
    private static MqttUnsubscribe delegate(
            @NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new MqttUnsubscribe(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static Mqtt3UnsubscribeView of(
            @NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new Mqtt3UnsubscribeView(delegate(topicFilters));
    }

    @NotNull
    public static Mqtt3UnsubscribeView of(@NotNull final MqttUnsubscribe delegate) {
        return new Mqtt3UnsubscribeView(delegate);
    }

    private final MqttUnsubscribe delegate;

    private Mqtt3UnsubscribeView(@NotNull final MqttUnsubscribe delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return delegate.getTopicFilters();
    }

    @NotNull
    public MqttUnsubscribe getDelegate() {
        return delegate;
    }
}
