package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.MqttServerConnectionDataImpl;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.NewMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeEncoder extends Mqtt5WrappedMessageEncoder<MqttUnsubscribe, MqttUnsubscribeWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttUnsubscribe, MqttUnsubscribeWrapper, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>>
            PROVIDER = NewMqttWrappedMessageEncoderProvider.create(Mqtt5UnsubscribeEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttTopicFilterImpl> topicFilters = message.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttUnsubscribeWrapper wrapper) {
        return Mqtt5UnsubscribeWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5UnsubscribeWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttUnsubscribeWrapper, MqttUnsubscribe, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>, Mqtt5UnsubscribeEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttUnsubscribeWrapper, MqttUnsubscribe, Mqtt5UnsubscribeEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5UnsubscribeWrapperEncoder::new);

        private static final int FIXED_HEADER = (Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4) | 0b0010;

        @Override
        public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
            final int maximumPacketSize = MqttServerConnectionDataImpl.getMaximumPacketSize(channel);

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeByte(FIXED_HEADER);
            MqttVariableByteInteger.encode(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeShort(message.getPacketIdentifier());
            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            MqttVariableByteInteger.encode(propertyLength(maximumPacketSize), out);
            encodeOmissibleProperties(maximumPacketSize, out);
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ImmutableList<MqttTopicFilterImpl> topicFilters = message.getWrapped().getTopicFilters();
            for (int i = 0; i < topicFilters.size(); i++) {
                topicFilters.get(i).to(out);
            }
        }

    }

}
