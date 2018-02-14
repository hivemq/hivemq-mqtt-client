package org.mqttbee.api.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import org.mqttbee.api.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.mqttbee.api.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5QoS2ControlProvider {

    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubRecBuilder pubAckBuilder);

    void onPubRec(@NotNull Mqtt5PubRec pubRec, @NotNull Mqtt5PubRelBuilder pubRelBuilder);

    void onPubRel(@NotNull Mqtt5PubRel pubRel, @NotNull Mqtt5PubCompBuilder pubCompBuilder);

    void onPubComp(@NotNull Mqtt5PubComp pubComp);

}
