package org.mqttbee.mqtt5.ioc;

import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt3Disconnecter;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5Disconnecter;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnecter;
import org.mqttbee.mqtt5.handler.publish.MqttSubscriptionFlowTree;
import org.mqttbee.mqtt5.handler.publish.MqttSubscriptionFlows;

import javax.inject.Named;

/**
 * @author Silvio Giebl
 */
@Module
public abstract class ChannelModule {

    @Provides
    @ChannelScope
    static MqttDisconnecter provideDisconnecter(
            final MqttClientData clientData, final Lazy<Mqtt5Disconnecter> mqtt5Disconnecter,
            final Lazy<Mqtt3Disconnecter> mqtt3Disconnecter) {

        switch (clientData.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5Disconnecter.get();
            case MQTT_3_1_1:
                return mqtt3Disconnecter.get();
            default:
                throw new IllegalStateException();
        }
    }

    @Provides
    @ChannelScope
    @Named("incomingPublish")
    static Scheduler.Worker provideIncomingPublishWorker(final MqttClientData clientData) {
        return clientData.getExecutorConfig().getRxJavaScheduler().createWorker();
    }

    @Binds
    abstract MqttSubscriptionFlows provideSubscriptionFlows(final MqttSubscriptionFlowTree tree);

}
