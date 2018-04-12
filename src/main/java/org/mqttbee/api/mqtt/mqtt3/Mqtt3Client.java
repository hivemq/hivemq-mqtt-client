package org.mqttbee.api.mqtt.mqtt3;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3Client extends MqttClient {

    @NotNull
    Single<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    @NotNull
    Flowable<Mqtt3Publish> subscribe(@NotNull Mqtt3Subscribe subscribe); // TODO temp

    @NotNull
    Flowable<Mqtt3Publish> remainingPublishes();

    @NotNull
    Flowable<Mqtt3Publish> allPublishes();

    @NotNull
    Completable unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    @NotNull
    Flowable<Mqtt3PublishResult> publish(@NotNull Flowable<Mqtt3Publish> publishFlowable);

    @NotNull
    Completable disconnect();

    @NotNull
    @Override
    Mqtt3ClientData getClientData();

}
