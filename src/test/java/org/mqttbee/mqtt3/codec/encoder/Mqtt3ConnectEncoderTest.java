package org.mqttbee.mqtt3.codec.encoder;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt3.message.connect.Mqtt3ConnectImpl;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Mqtt3ConnectEncoderTest {

    private static final byte[] EXAMPLE_CONNECT = {
            // FIXED HEADER
            // packet type and flags
            0x10,
            // remaining length (16 Bytes)
            0x10,
            // VARIABLE HEADER
            // protocol name
            0x00, //msb
            0x04, //lsb
            'M', 'Q', 'T', 'T',
            //protocol level
            0x04,
            //connect flags (only clean sesion is set)
            0b0000_0010,
            //keep alive (60)
            0x00, 0x3c,
            //clientId
            0x00, 0x04, 'T', 'E', 'S', 'T'
    };
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Encoder());
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    @Test
    void encode_SUCCESS() throws Exception {
        Mqtt5UTF8String username = null;
        byte[] password = null;
        Mqtt3PublishImpl will = null;
        Mqtt5ClientIdentifier identifier = Mqtt5ClientIdentifier.from("TEST");
        Mqtt3ConnectImpl connect = new Mqtt3ConnectImpl(username, password, will, true, 60, identifier);
        encode(EXAMPLE_CONNECT, connect);
    }


    @Test
    void encodedLength_SUCESS() {
        Mqtt5UTF8String username = null;
        byte[] password = null;
        Mqtt3PublishImpl will = null;
        Mqtt5ClientIdentifier identifier = Mqtt5ClientIdentifier.from("TEST");
        Mqtt3ConnectImpl connect = new Mqtt3ConnectImpl(username, password, will, true, 60, identifier);
        assertEquals(18, connect.encodedLength());
    }




    @Test
    void test_SUCCESS_WITH_WILL_WITH_PAHO() throws Exception {

        String clientId = "Test123";
        boolean cleansession = true;
        int keepAlive = 120;
        String username = null;
        String password = null;
        String willTopic = "my/last/will";
        String myLastWill = "mylastwillpayload";
        boolean isRetained = false;
        int qosWill = 1;


        //PAHO
        MqttMessage will = new MqttMessage(myLastWill.getBytes());
        will.setQos(qosWill);
        MqttConnect pahoConnect = new MqttConnect(clientId, 4, cleansession, keepAlive, username,
                password == null ? "".toCharArray() : password.toCharArray(), will, willTopic);

        byte[] expected = Bytes.concat(pahoConnect.getHeader(), pahoConnect.getPayload());


        Mqtt3PublishImpl willMessage =
                new Mqtt3PublishImpl(myLastWill.getBytes(), Mqtt5Topic.from(willTopic), Mqtt5QoS.fromCode(qosWill),
                        isRetained, false, -1);

        Mqtt5UTF8String usernameUTF8 = null;
        if (username != null) {
            usernameUTF8 = Mqtt5UTF8String.from(username);
        }

        final byte[] passwordBytes = password == null ? null : password.getBytes();


        Mqtt3ConnectImpl connect =
                new Mqtt3ConnectImpl(usernameUTF8, passwordBytes, willMessage, cleansession, keepAlive,
                        Mqtt5ClientIdentifier.from(clientId));


        encode(expected, connect);
    }


    /**
     * Pseudo test to assure we get the right bytes from paho methods
     *
     * @throws Exception
     */
    @Test
    void test_PAHO_GETPAYLOAD_METHOD() throws Exception {
        byte[] expected = EXAMPLE_CONNECT;
        MqttConnect pahoConnect = new MqttConnect("TEST", 4, true, 60, null, null, null, null);
        byte[] actual = Bytes.concat(pahoConnect.getHeader(), pahoConnect.getPayload());
        assertArrayEquals(expected, actual);

    }


    private void encode(final byte[] expected, final Mqtt3ConnectImpl connect) {
        channel.writeOutbound(connect);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertArrayEquals(expected, actual);
    }


}