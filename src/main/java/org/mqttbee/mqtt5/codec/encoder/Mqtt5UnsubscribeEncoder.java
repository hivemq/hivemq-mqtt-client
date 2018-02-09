package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeWrapper;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeEncoder extends Mqtt5WrappedMessageEncoder<Mqtt5UnsubscribeImpl, Mqtt5UnsubscribeWrapper> {

    public static final Function<Mqtt5UnsubscribeImpl, Mqtt5UnsubscribeEncoder> PROVIDER = Mqtt5UnsubscribeEncoder::new;

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    private Mqtt5UnsubscribeEncoder(@NotNull final Mqtt5UnsubscribeImpl message) {
        super(message);
    }

    @Override
    int calculateEncodedRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<Mqtt5TopicFilterImpl> topicFilters = message.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        return remainingLength;
    }

    @Override
    int calculateEncodedPropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @Override
    public Function<Mqtt5UnsubscribeWrapper, ? extends Mqtt5MessageWrapperEncoder<Mqtt5UnsubscribeWrapper>> wrap() {
        return Mqtt5UnsubscribeWrapperEncoder.PROVIDER;
    }


    public static class Mqtt5UnsubscribeWrapperEncoder extends Mqtt5MessageWrapperEncoder<Mqtt5UnsubscribeWrapper> {

        public static final Function<Mqtt5UnsubscribeWrapper, Mqtt5UnsubscribeWrapperEncoder> PROVIDER =
                Mqtt5UnsubscribeWrapperEncoder::new;

        private static final int FIXED_HEADER = (Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4) | 0b0010;

        private Mqtt5UnsubscribeWrapperEncoder(@NotNull final Mqtt5UnsubscribeWrapper wrapper) {
            super(wrapper);
        }

        @Override
        public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
            final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeByte(FIXED_HEADER);
            Mqtt5DataTypes.encodeVariableByteInteger(encodedRemainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeShort(message.getPacketIdentifier());
            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            Mqtt5DataTypes.encodeVariableByteInteger(encodedPropertyLength(maximumPacketSize), out);
            encodeUserProperties(maximumPacketSize, out);
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ImmutableList<Mqtt5TopicFilterImpl> topicFilters = message.getWrapped().getTopicFilters();
            for (int i = 0; i < topicFilters.size(); i++) {
                topicFilters.get(i).to(out);
            }
        }

    }

}
