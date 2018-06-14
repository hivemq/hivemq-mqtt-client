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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscriptionBuilder {

    private MqttTopicFilterImpl topicFilter;
    private MqttQoS qos;
    private boolean noLocal = Mqtt5Subscription.DEFAULT_NO_LOCAL;
    private Mqtt5RetainHandling retainHandling = Mqtt5Subscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = Mqtt5Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

    Mqtt5SubscriptionBuilder() {
    }

    @NotNull
    public Mqtt5SubscriptionBuilder topicFilter(@NotNull final String topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder topicFilter(@NotNull final MqttTopicFilter topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder qos(@NotNull final MqttQoS qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder noLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder retainHandling(@NotNull final Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Preconditions.checkNotNull(retainHandling);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder retainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return this;
    }

    @NotNull
    public Mqtt5Subscription build() {
        Preconditions.checkNotNull(topicFilter);
        Preconditions.checkNotNull(qos);
        Preconditions.checkArgument(!(topicFilter.isShared() && noLocal));
        return new MqttSubscription(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
    }

}
