/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.ioc;

import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import javax.inject.Named;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.disconnect.Mqtt3Disconnecter;
import org.mqttbee.mqtt.handler.disconnect.Mqtt5Disconnecter;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnecter;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlowTree;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlows;

/** @author Silvio Giebl */
@Module
public abstract class ChannelModule {

    @Provides
    @ChannelScope
    static MqttDisconnecter provideDisconnecter(
            final MqttClientData clientData,
            final Lazy<Mqtt5Disconnecter> mqtt5Disconnecter,
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
    static Scheduler.Worker provideIncomingPublishRxEventLoop(final MqttClientData clientData) {
        return clientData.getExecutorConfig().getRxJavaScheduler().createWorker();
    }

    @Binds
    abstract MqttSubscriptionFlows provideSubscriptionFlows(final MqttSubscriptionFlowTree tree);
}
