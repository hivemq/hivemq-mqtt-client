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
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeBuilder<P> extends FluentBuilder<Mqtt5Subscribe, P> {

    private final ImmutableList.Builder<MqttSubscription> subscriptionBuilder;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5SubscribeBuilder(@Nullable final Function<Mqtt5Subscribe, P> parentConsumer) {
        super(parentConsumer);
        subscriptionBuilder = ImmutableList.builder();
    }

    Mqtt5SubscribeBuilder(@NotNull final Mqtt5Subscribe subscribe) {
        super(null);
        final MqttSubscribe subscribeImpl =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, MqttSubscribe.class);
        final ImmutableList<MqttSubscription> subscriptions = subscribeImpl.getSubscriptions();
        subscriptionBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionBuilder.addAll(subscriptions);
    }

    @NotNull
    public Mqtt5SubscribeBuilder<P> addSubscription(@NotNull final Mqtt5Subscription subscription) {
        subscriptionBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(subscription, MqttSubscription.class));
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<Mqtt5SubscribeBuilder<P>> addSubscription() {
        return new Mqtt5SubscriptionBuilder<>(this::addSubscription);
    }

    @NotNull
    public Mqtt5SubscribeBuilder<P> userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<? extends Mqtt5SubscribeBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    @Override
    public Mqtt5Subscribe build() {
        final ImmutableList<MqttSubscription> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return new MqttSubscribe(subscriptions, userProperties);
    }

}
