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

package com.hivemq.client2.internal.mqtt.mqtt3;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.MqttRxClient;
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt3.exceptions.Mqtt3MessageException;
import com.hivemq.client2.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client2.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client2.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client2.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client2.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client2.rx.FlowableWithSingleSplit;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * @author David Katz
 */
@SuppressWarnings("NullabilityAnnotations")
class Mqtt3RxClientViewExceptionsTest {

    private MqttRxClient mqtt5Client;
    private Mqtt3RxClientView mqtt3Client;

    @BeforeEach
    void setUp() {
        mqtt5Client = mock(MqttRxClient.class);
        when(mqtt5Client.getConfig()).thenReturn(mock(MqttClientConfig.class));
        mqtt3Client = new Mqtt3RxClientView(mqtt5Client);
    }

    @Test
    void connect() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.connect(any())).willReturn(Single.error(mqtt5MessageException));

        final Mqtt3Connect connect = Mqtt3Connect.builder().build();
        assertMqtt3Exception(() -> mqtt3Client.connect(connect).ignoreElement().blockingAwait(), mqtt5MessageException);
    }

    @Test
    void subscribe() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.subscribe(any())).willReturn(Single.error(mqtt5MessageException));

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .addSubscription(Mqtt3Subscription.builder().topicFilter("topic").maxQos(MqttQos.AT_LEAST_ONCE).build())
                .build();
        assertMqtt3Exception(() -> mqtt3Client.subscribe(subscribe).ignoreElement().blockingAwait(),
                mqtt5MessageException);
    }

    @Test
    void subscribeWithStream() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.subscribePublishes(any(), anyBoolean())).willReturn(
                new FlowableWithSingleSplit<>(Flowable.error(mqtt5MessageException), Mqtt5Publish.class,
                        Mqtt5SubAck.class));

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .addSubscription(Mqtt3Subscription.builder().topicFilter("topic").maxQos(MqttQos.AT_LEAST_ONCE).build())
                .build();
        assertMqtt3Exception(
                () -> mqtt3Client.subscribePublishes(subscribe, false).blockingSubscribe(), mqtt5MessageException);
    }

    @ParameterizedTest
    @EnumSource(MqttGlobalPublishFilter.class)
    void publishes(final @NotNull MqttGlobalPublishFilter filter) {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.publishes(filter, false)).willReturn(Flowable.error(mqtt5MessageException));

        assertMqtt3Exception(() -> mqtt3Client.publishes(filter, false).blockingSubscribe(), mqtt5MessageException);
    }

    @Test
    void unsubscribe() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.unsubscribe(any())).willReturn(Single.error(mqtt5MessageException));

        final Mqtt3Unsubscribe unsubscribe = Mqtt3Unsubscribe.builder().addTopicFilter("topic").build();
        assertMqtt3Exception(
                () -> mqtt3Client.unsubscribe(unsubscribe).ignoreElement().blockingAwait(), mqtt5MessageException);
    }

    @Test
    void publish() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.publish(any(), any())).willReturn(Flowable.error(mqtt5MessageException));

        final Flowable<Mqtt3Publish> publish =
                Flowable.just(Mqtt3Publish.builder().topic("topic").qos(MqttQos.AT_LEAST_ONCE).build());
        assertMqtt3Exception(() -> mqtt3Client.publish(publish).blockingSubscribe(), mqtt5MessageException);
    }

    @Test
    void disconnect() {
        final Mqtt5MessageException mqtt5MessageException =
                new Mqtt5DisconnectException(MqttDisconnect.DEFAULT, "reason from original exception");
        given(mqtt5Client.disconnect(any())).willReturn(Completable.error(mqtt5MessageException));

        assertMqtt3Exception(() -> mqtt3Client.disconnect().blockingAwait(), mqtt5MessageException);
    }

    private void assertMqtt3Exception(
            @NotNull final Executable executable, @NotNull final Mqtt5MessageException mqtt5MessageException) {

        final RuntimeException runtimeException = assertThrows(RuntimeException.class, executable);
        assertTrue(runtimeException instanceof Mqtt3MessageException);
        final Mqtt3MessageException mqtt3MessageException = (Mqtt3MessageException) runtimeException;
        assertEquals(mqtt5MessageException.getMqttMessage().getType().getCode(),
                mqtt3MessageException.getMqttMessage().getType().getCode());
        assertEquals(mqtt5MessageException.getMessage(), mqtt3MessageException.getMessage());
    }

}
