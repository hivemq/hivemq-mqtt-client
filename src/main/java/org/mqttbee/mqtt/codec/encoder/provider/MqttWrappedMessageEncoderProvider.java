package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

import java.util.function.Supplier;

/**
 * Provider for a {@link MqttMessageEncoderApplier} for a wrapped MQTT message.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @param <P> the type of the encoder provider for the MQTT message wrapper.
 * @author Silvio Giebl
 */
public class MqttWrappedMessageEncoderProvider< //
        M extends MqttWrappedMessage<M, W, P>, //
        W extends MqttMessageWrapper<W, M, P>, //
        P extends MqttMessageEncoderProvider<W>> //
        implements Supplier<MqttWrappedMessageEncoderApplier<M, W>> {

    @NotNull
    public static < //
            M extends MqttWrappedMessage<M, W, MqttMessageEncoderProvider<W>>, //
            W extends MqttMessageWrapper<W, M, MqttMessageEncoderProvider<W>>> //
    MqttWrappedMessageEncoderProvider<M, W, MqttMessageEncoderProvider<W>> create(
            @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider) {

        return new MqttWrappedMessageEncoderProvider<>(provider, new MqttMessageWrapperEncoderProvider<>());
    }

    private final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider;
    private final P wrapperEncoderProvider;

    public MqttWrappedMessageEncoderProvider(
            @NotNull final Supplier<MqttWrappedMessageEncoderApplier<M, W>> provider,
            @NotNull final P wrapperEncoderProvider) {

        this.provider = provider;
        this.wrapperEncoderProvider = wrapperEncoderProvider;
    }

    /**
     * @return the applier for a encoder for a wrapped MQTT message.
     */
    @Override
    public MqttWrappedMessageEncoderApplier<M, W> get() {
        return provider.get();
    }

    /**
     * @return the encoder provider for the MQTT message wrapper.
     */
    @NotNull
    public P getWrapperEncoderProvider() {
        return wrapperEncoderProvider;
    }

}
