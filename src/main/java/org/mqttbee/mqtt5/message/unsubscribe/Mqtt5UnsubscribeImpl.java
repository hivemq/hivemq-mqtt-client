package org.mqttbee.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5WrappedMessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeImpl extends Mqtt5WrappedMessage<Mqtt5UnsubscribeImpl, Mqtt5UnsubscribeWrapper>
        implements Mqtt5Unsubscribe {

    private final ImmutableList<Mqtt5TopicFilterImpl> topicFilters;

    public Mqtt5UnsubscribeImpl(
            @NotNull final ImmutableList<Mqtt5TopicFilterImpl> topicFilters,
            @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5UnsubscribeImpl, ? extends Mqtt5WrappedMessageEncoder<Mqtt5UnsubscribeImpl, Mqtt5UnsubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.topicFilters = topicFilters;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5TopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    @Override
    protected Mqtt5UnsubscribeImpl getCodable() {
        return this;
    }

    public Mqtt5UnsubscribeWrapper wrap(final int packetIdentifier) {
        return new Mqtt5UnsubscribeWrapper(this, packetIdentifier);
    }

}
