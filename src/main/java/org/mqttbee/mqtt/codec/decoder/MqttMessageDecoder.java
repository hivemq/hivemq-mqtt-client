package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Decoder for a MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessageDecoder {

    /**
     * Decodes a MQTT message from the given byte buffer which was read from the given channel.
     *
     * @param flags                the flags of the fixed header.
     * @param in                   the byte buffer which contains the encoded message without the fixed header.
     * @param clientConnectionData the client data.
     * @return the decoded MQTT message or null if there are not enough byte in the byte buffer.
     * @throws MqttDecoderException if the byte buffer did not contain a valid encoded MQTT message.
     */
    @Nullable
    MqttMessage decode(int flags, @NotNull ByteBuf in, @NotNull MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException;

}
