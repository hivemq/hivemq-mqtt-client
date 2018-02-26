package org.mqttbee.mqtt.codec.encoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.MqttServerConnectionDataImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
public class AbstractMqtt5EncoderTest {

    private final boolean connected;
    private final MqttClientDataImpl clientData;

    protected EmbeddedChannel channel;

    protected AbstractMqtt5EncoderTest(final boolean connected) {
        this.connected = connected;
        clientData = new MqttClientDataImpl(MqttVersion.MQTT_5_0,
                Objects.requireNonNull(MqttClientIdentifierImpl.from("test")), "localhost", 1883, false, false, false,
                null, 0);
    }

    @BeforeEach
    void setUp() {
        createChannel();
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    private void createChannel() {
        channel = new EmbeddedChannel(new MqttEncoder());
        clientData.to(channel);
        if (connected) {
            createServerConnectionData(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
        }
    }

    protected void createServerConnectionData(final int maximumPacketSize) {
        clientData.setServerConnectionData(
                new MqttServerConnectionDataImpl(10, 3, maximumPacketSize, MqttQoS.EXACTLY_ONCE, true, true, true,
                        true));
    }

}
