package org.mqttbee.mqtt5;

import io.netty.util.AttributeKey;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.publish.Mqtt5TopicAliasMapping;

/**
 * @author Silvio Giebl
 */
public class ChannelAttributes {

    public static final AttributeKey<Integer> INCOMING_RECEIVE_MAXIMUM =
            AttributeKey.valueOf("restriction.in.receive.max");
    public static final AttributeKey<Integer> INCOMING_RECEIVE_COUNTER =
            AttributeKey.valueOf("restriction.in.receive.counter");
    public static final AttributeKey<Mqtt5Topic[]> INCOMING_TOPIC_ALIAS_MAPPING =
            AttributeKey.valueOf("in.topic.alias.mapping");
    public static final AttributeKey<Long> INCOMING_MAXIMUM_PACKET_SIZE = AttributeKey.valueOf("in.packet.size.max");

    public static final AttributeKey<Integer> OUTGOING_RECEIVE_MAXIMUM =
            AttributeKey.valueOf("restriction.out.receive.max");
    public static final AttributeKey<Integer> OUTGOING_RECEIVE_COUNTER =
            AttributeKey.valueOf("restriction.out.receive.counter");
    public static final AttributeKey<Mqtt5TopicAliasMapping> OUTGOING_TOPIC_ALIAS_MAPPING =
            AttributeKey.valueOf("out.topic.alias.mapping");
    public static final AttributeKey<Long> OUTGOING_MAXIMUM_PACKET_SIZE = AttributeKey.valueOf("out.packet.size.max");
//    public static final AttributeKey<Integer> RESTRICTION_OUTGOING_MAXIMUM_QOS =
//            AttributeKey.valueOf("restriction.out.qos.max");
//    public static final AttributeKey<Boolean> RESTRICTION_OUTGOING_RETAIN_AVAILABLE =
//            AttributeKey.valueOf("restriction.out.retain.available");
//    public static final AttributeKey<Boolean> RESTRICTION_OUTGOING_WILDCARD_SUBSCRIPTION_AVAILABLE =
//            AttributeKey.valueOf("restriction.out.wildcard.subscription.available");
//    public static final AttributeKey<Boolean> RESTRICTION_OUTGOING_SUBSCRIPTION_IDENTIFIER_AVAILABLE =
//            AttributeKey.valueOf("restriction.out.subscription.identifier.available");
//    public static final AttributeKey<Boolean> RESTRICTION_OUTGOING_SHARED_SUBSCRIPTION_AVAILABLE =
//            AttributeKey.valueOf("restriction.out.shared.subscription.available");

    public static final AttributeKey<Boolean> PROBLEM_INFORMATION_REQUESTED =
            AttributeKey.valueOf("problem.information.requested");

    public static final AttributeKey<Boolean> SEND_REASON_STRING = AttributeKey.valueOf("reason.string.send");

    public static final AttributeKey<Boolean> VALIDATE_PAYLOAD_FORMAT =
            AttributeKey.valueOf("payload.format.indicator.validate");

    private ChannelAttributes() {
    }

}
