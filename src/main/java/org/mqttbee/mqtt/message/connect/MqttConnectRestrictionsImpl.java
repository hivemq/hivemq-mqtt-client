package org.mqttbee.mqtt.message.connect;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnectRestrictionsImpl implements Mqtt5ConnectRestrictions {

    @NotNull
    public static final MqttConnectRestrictionsImpl DEFAULT =
            new MqttConnectRestrictionsImpl(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM,
                    DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);

    private final int receiveMaximum;
    private final int topicAliasMaximum;
    private final int maximumPacketSize;

    public MqttConnectRestrictionsImpl(
            final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize) {

        this.receiveMaximum = receiveMaximum;
        this.topicAliasMaximum = topicAliasMaximum;
        this.maximumPacketSize = maximumPacketSize;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

}
