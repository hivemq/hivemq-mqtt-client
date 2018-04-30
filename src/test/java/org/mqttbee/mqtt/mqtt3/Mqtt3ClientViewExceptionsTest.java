/**
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
 */
package org.mqttbee.mqtt.mqtt3;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;
import org.mqttbee.rx.FlowableWithSingle;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

/**
 * @author David Katz
 */
class Mqtt3ClientViewExceptionsTest {

    private Mqtt5ClientImpl mqtt5Client;
    private Mqtt3ClientView mqtt3Client;

    @BeforeEach
    void setUp() {
        mqtt5Client = mock(Mqtt5ClientImpl.class);
        mqtt3Client = new Mqtt3ClientView(mqtt5Client);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void connect() {
        given(mqtt5Client.connect(any())).willAnswer(
            invocation -> Single.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        Mqtt3Connect connect = Mqtt3Connect.builder().build();
        assertMqtt3Exception(() -> mqtt3Client.connect(connect).blockingGet());
    }

    @Test
    void subscribe() {
        given(mqtt5Client.subscribe(any())).willAnswer(
            invocation -> Flowable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().build();
        mqtt3Client.subscribe(subscribe).blockingSubscribe();
        assertMqtt3Exception(() -> mqtt3Client.subscribe(subscribe).blockingSubscribe());
    }

    @Test
    void remainingPublishes() {
        given(mqtt5Client.remainingPublishes()).willAnswer(
            invocation -> Flowable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        assertMqtt3Exception(() -> mqtt3Client.remainingPublishes().blockingSubscribe());
    }

    @Test
    void allPublishes() {
        given(mqtt5Client.allPublishes()).willAnswer(
            invocation -> Flowable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        assertMqtt3Exception(() -> mqtt3Client.allPublishes().blockingSubscribe());

    }

    @Test
    void unsubscribe() {
//        given(mqtt5Client.subscribe(any())).willAnswer(
//            invocation -> Flowable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        given(mqtt5Client.unsubscribe(any())).willAnswer(
            invocation -> Single.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        Mqtt3Unsubscribe unsubscribe = Mqtt3Unsubscribe.builder().addTopicFilter("topic").build();
        assertMqtt3Exception(() -> mqtt3Client.unsubscribe(unsubscribe).blockingAwait());
    }

    @Test
    void publish() {
        given(mqtt5Client.publish(any())).willAnswer(
            invocation -> Flowable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        Flowable<Mqtt3Publish> publish = Flowable.just(Mqtt3Publish.builder().build());
        assertMqtt3Exception(() -> mqtt3Client.publish(publish).blockingSubscribe());
    }

    @Test
    void disconnect() {
        given(mqtt5Client.disconnect(any())).willAnswer(
            invocation -> Completable.error(new Mqtt5MessageException(Mqtt5Connect.builder().build(), "reason from original exception")));

        assertMqtt3Exception(() -> mqtt3Client.disconnect().blockingAwait());
    }

    void assertMqtt3Exception(Executable executable) {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, executable);
        assertThat(runtimeException.getCause(), instanceOf(Mqtt3MessageException.class));
    }
}
