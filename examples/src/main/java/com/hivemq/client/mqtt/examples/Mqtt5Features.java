/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.mqtt.examples;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;

import java.util.concurrent.CountDownLatch;

/**
 * Shows MQTT 5 features like session expiry, message expiry, user properties, topic aliases, flow control.
 *
 * @author Silvio Giebl
 */
// @formatter:off
public class Mqtt5Features {

    public static void main(final String[] args)throws InterruptedException {

        final Mqtt5AsyncClient client = Mqtt5Client.builder()
                .serverHost("broker.hivemq.com")
                .automaticReconnect()
                .buildAsync();

        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(false)          // resume a previous session
                .sessionExpiryInterval(30)  // keep session state for 30s
                .restrictionsWith()
                    .receiveMaximum(10)             // receive max. 10 concurrent messages
                    .sendMaximum(10)                // send max. 10 concurrent messages
                    .maximumPacketSize(10_240)      // receive messages with max size of 10KB
                    .sendMaximumPacketSize(10_240)  // send messages with max size of 10KB
                    .topicAliasMaximum(0)           // the server should not use topic aliases
                    .sendTopicAliasMaximum(8)       // use up to 8 aliases for the most used topics (automatically traced)
                    .applyRestrictions()
                .willPublishWith()
                    .topic("demo/topic/will")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload("rip".getBytes())
                    .contentType("text/plain")  // our payload is text
                    .messageExpiryInterval(120) // not so important, expire message after 2min if can not be delivered
                    .delayInterval(30)          // delay sending out the will message so we can try to reconnect immediately
                    .userPropertiesWith()           // add some user properties to the will message
                        .add("sender", "demo-sender-1")
                        .add("receiver", "you")
                        .applyUserProperties()
                    .applyWillPublish()
                .send();

        System.out.println("connected " + connAck);


        final Mqtt5SubAck subAck = client.subscribeWith()
                .topicFilter("demo/topic/a")
                .noLocal(true)                                      // we do not want to receive our own message
                .retainHandling(Mqtt5RetainHandling.DO_NOT_SEND)    // do not send retained messages
                .retainAsPublished(true)                            // keep the retained flag as it was published
                .callback(publish -> System.out.println("received message: " + publish))
                .send().join();

        System.out.println("subscribed " + subAck);


        client.toBlocking().publishWith()
                .topic("demo/topic/a")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("payload".getBytes())
                .retain(true)
                .contentType("text/plain")  // our payload is text
                .messageExpiryInterval(120) // not so important, expire message after 2min if can not be delivered
                .userPropertiesWith()           // add some user properties to the message
                    .add("sender", "demo-sender-1")
                    .add("receiver", "you")
                    .applyUserProperties()
                .send();

        System.out.println("published: we do not receive our own messages");


        // setup a latch to wait for 1 message
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.publishes(MqttGlobalPublishFilter.ALL, publish -> countDownLatch.countDown());


        final Mqtt5BlockingClient client2 = Mqtt5Client.builder().serverHost("broker.hivemq.com").buildBlocking();
        client2.connect();
        client2.publishWith()
                .topic("demo/topic/a")
                .retain(true)
                .userPropertiesWith()
                    .add("sender", "demo-sender-2")
                    .add("receiver", "you")
                    .applyUserProperties()
                .send();
        client2.disconnect();

        System.out.println("client2 published: waiting for message to be received");
        countDownLatch.await();
        System.out.println("received message from client2: see the user property sender, also see that retain=true as requested");


        client.toBlocking().disconnectWith()
                .reasonCode(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE) // send the will message
                .sessionExpiryInterval(0)                                           // we want to clear the session
                .send();

        System.out.println("disconnected");


        System.exit(0);
    }
}
