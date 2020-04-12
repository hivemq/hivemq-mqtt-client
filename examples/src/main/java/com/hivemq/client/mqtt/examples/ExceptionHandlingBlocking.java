/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt.examples;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;

/**
 * Example how to handle blocking exceptions
 *
 * @author Adam Raźniewski
 */
public class ExceptionHandlingBlocking {

    public static void main(final String[] args){
        // Not existing mqtt broker on google.com - should return error
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost("google.com")
                .serverPort(80)
                .buildBlocking();
        try {
            client.connect();
        } catch(Mqtt5DisconnectException e) {
            System.out.println("Catch disconnect exception exception");
            System.out.println(e.getMqttMessage().getReasonString().get());
        }

    }


}
