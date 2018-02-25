package org.mqttbee.mqtt.message.subscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3SubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscriptionImpl;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SubscribeView implements Mqtt3Subscribe {

    public static MqttSubscribeImpl wrapped(
            @NotNull final ImmutableList<MqttSubscriptionImpl> subscriptions) {

        return new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt3SubscribeEncoder.PROVIDER);
    }

    private static ImmutableList<Mqtt3SubscriptionView> wrapSubscriptions(
            @NotNull final ImmutableList<MqttSubscriptionImpl> subscriptions) {

        final ImmutableList.Builder<Mqtt3SubscriptionView> builder =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(new Mqtt3SubscriptionView(subscriptions.get(i)));
        }
        return builder.build();
    }

    public static Mqtt3SubscribeView create(
            @NotNull final ImmutableList<MqttSubscriptionImpl> subscriptions) {

        return new Mqtt3SubscribeView((wrapped(subscriptions)));
    }

    private final MqttSubscribeImpl wrapped;

    private Mqtt3SubscribeView(@NotNull final MqttSubscribeImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<? extends Mqtt3Subscription> getSubscriptions() {
        return wrapSubscriptions(wrapped.getSubscriptions());
    }

    @NotNull
    public MqttSubscribeImpl getWrapped() {
        return wrapped;
    }

}
