package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.mqttbee.util.collections.ScNodeList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlow extends MqttIncomingPublishFlow {

    public static final int TYPE_ALL_SUBSCRIPTIONS = 0;
    public static final int TYPE_ALL_PUBLISHES = 1;
    public static final int TYPE_REMAINING_PUBLISHES = 2;
    public static final int TYPE_COUNT = 3;

    private final int type;
    private ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle;

    public MqttGlobalIncomingPublishFlow(
            @NotNull final Subscriber<? super MqttSubscribeResult> actual,
            @NotNull final MqttIncomingPublishService incomingPublishService, final int type) {

        super(actual, incomingPublishService);
        this.type = type;
    }

    @Override
    void runRemoveOnCancel() {
        incomingPublishService.getIncomingPublishFlows().cancelGlobal(this);
    }

    public int getType() {
        return type;
    }

    public void setHandle(@NotNull final ScNodeList.Handle<MqttGlobalIncomingPublishFlow> handle) {
        this.handle = handle;
    }

    public ScNodeList.Handle<MqttGlobalIncomingPublishFlow> getHandle() {
        return handle;
    }

}
