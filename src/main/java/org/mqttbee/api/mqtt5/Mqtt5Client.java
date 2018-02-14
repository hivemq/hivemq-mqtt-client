package org.mqttbee.api.mqtt5;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.rx.FlowableWithSingle;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Client {

    @NotNull
    Single<Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    @NotNull
    FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe(@NotNull Mqtt5Subscribe subscribe);

    @NotNull
    Flowable<Mqtt5Publish> remainingPublishes();

    @NotNull
    Flowable<Mqtt5Publish> allPublishes();

    @NotNull
    Single<Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    void publish(@NotNull Flowable<Mqtt5Publish> publishFlowable);

    @NotNull
    Completable reauth();

    @NotNull
    Completable disconnect(@NotNull Mqtt5Disconnect disconnect);

    @NotNull
    Mqtt5ClientData getClientData();

    @NotNull
    Mqtt5ServerData getServerData();

}
