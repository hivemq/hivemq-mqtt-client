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

package com.hivemq.client2.mqtt.examples;

import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5Publish;

/**
 * Shows how to implement a request/response pattern using response topic and correlation data.
 *
 * @author Silvio Giebl
 */
public class RequestResponse {

    public static void main(final String[] args) {
        final Mqtt5Client requester = Mqtt5Client.builder().serverHost("broker.hivemq.com").build();
        final Mqtt5Client responder = Mqtt5Client.builder().serverHost("broker.hivemq.com").build();

        requester.toBlocking().connect();
        responder.toBlocking().connect();

        responder.toRx()
                .publish(responder.toRx()
                        .subscribePublishesWith()
                        .topicFilter("request/topic")
                        .applySubscribe()
                        .map(requestPublish -> Mqtt5Publish.builder()
                                .topic(requestPublish.getResponseTopic().get())
                                .qos(requestPublish.getQos())
                                .payload("response".getBytes())
                                .correlationData(requestPublish.getCorrelationData().orElse(null))
                                .build()))
                .subscribe(); // this call is a reactive streams subscribe call, not an MQTT subscribe

        requester.toAsync()
                .subscribeWith()
                .topicFilter("response/topic")
                .callback(responsePublish -> System.out.println("received response"))
                .send()
                .thenCompose(subAck -> requester.toAsync()
                        .publishWith()
                        .topic("request/topic")
                        .responseTopic("response/topic")
                        .correlationData("1234".getBytes())
                        .qos(MqttQos.EXACTLY_ONCE)
                        .payload("request".getBytes())
                        .send());
    }
}
