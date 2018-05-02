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

package org.mqttbee.mqtt5.ioc;

import dagger.BindsInstance;
import dagger.Subcomponent;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderModule;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import org.mqttbee.mqtt.handler.auth.MqttReAuthHandler;
import org.mqttbee.mqtt.handler.connect.MqttDisconnectOnConnAckHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnecter;
import org.mqttbee.mqtt5.handler.publish.*;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt5.persistence.Mqtt5PersistenceModule;

/**
 * @author Silvio Giebl
 */
@Subcomponent(modules = {ChannelModule.class, MqttDecoderModule.class, Mqtt5PersistenceModule.class})
@ChannelScope
public interface ChannelComponent {

    AttributeKey<ChannelComponent> KEY = AttributeKey.valueOf("channel.component");

    @NotNull
    static ChannelComponent create(@NotNull final Channel channel, @NotNull final MqttClientData clientData) {
        final ChannelComponent channelComponent =
                MqttBeeComponent.INSTANCE.channelComponentBuilder().clientData(clientData).build();
        channel.attr(KEY).set(channelComponent);
        return channelComponent;
    }

    @NotNull
    static ChannelComponent get(@NotNull final Channel channel) {
        return channel.attr(KEY).get();
    }

    MqttClientData clientData();

    MqttDecoder decoder();

    MqttEncoder encoder();

    MqttDisconnecter disconnecter();

    MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler();

    MqttAuthHandler authHandler();

    MqttReAuthHandler reAuthHandler();

    MqttDisconnectOnAuthHandler disconnectOnAuthHandler();

    MqttDisconnectHandler disconnectHandler();

    MqttSubscriptionHandler subscriptionHandler();

    Mqtt5IncomingQoSHandler incomingQoSHandler();

    Mqtt5OutgoingQoSHandler outgoingQoSHandler();

    MqttIncomingPublishService incomingPublishService();

    MqttOutgoingPublishService outgoingPublishService();

    MqttPublishFlowables publishFlowables();


    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder clientData(MqttClientData clientData);

        ChannelComponent build();

    }

}
