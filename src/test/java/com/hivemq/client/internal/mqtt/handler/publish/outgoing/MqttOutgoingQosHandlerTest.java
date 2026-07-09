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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl.NO_USER_PROPERTIES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterization test for {@link MqttOutgoingQosHandler}.
 * <p>
 * The handler buffers outgoing PUBLISHes in a single-producer/single-consumer jctools queue ({@code onNext} offers,
 * {@code run} polls). This test locks that FIFO drain-to-the-wire behavior so a change of the underlying queue
 * implementation (e.g. the {@code SpscUnboundedArrayQueue} to {@code SpscUnboundedAtomicArrayQueue} swap for Java 25)
 * can be verified to keep identical semantics.
 *
 * @author David Sondermann
 */
class MqttOutgoingQosHandlerTest {

    // QoS 0 consumes no packet identifiers, so each run() pass writes up to sendMaximum publishes
    private static final int LARGE_SEND_MAXIMUM = 100; // >= PUBLISH_COUNT: single run() pass
    private static final int SMALL_SEND_MAXIMUM = 2;   //  < PUBLISH_COUNT: forces multiple run() passes / reschedules
    private static final int PUBLISH_COUNT = 5;

    @Test
    void run_drains_queued_qos0_publishes_in_fifo_order_single_pass() {
        drainAndAssertFifo(LARGE_SEND_MAXIMUM);
    }

    @Test
    void run_preserves_fifo_order_across_multiple_passes_when_send_maximum_is_small() {
        // sendMaximum < publishCount: run() writes sendMaximum, then self-reschedules until the queue is drained
        drainAndAssertFifo(SMALL_SEND_MAXIMUM);
    }

    private static void drainAndAssertFifo(final int sendMaximum) {
        final EmbeddedChannel channel = new EmbeddedChannel();
        try {
            final MqttClientConfig clientConfig = mock(MqttClientConfig.class);
            // route every flow's event loop to the channel's, so the run() scheduled by onNext executes
            // deterministically via runPendingTasks() on the same single thread
            when(clientConfig.acquireEventLoop()).thenReturn(channel.eventLoop());

            final MqttOutgoingQosHandler handler = new MqttOutgoingQosHandler(clientConfig);
            channel.pipeline().addLast(MqttOutgoingQosHandler.NAME, handler);

            // real connection config (matches AbstractMqttEncoderTest); no send topic alias mapping
            final MqttClientConnectionConfig connectionConfig = new MqttClientConnectionConfig(
                    MqttClientTransportConfigImpl.DEFAULT, 10, true, true, 0, false, false, null, 10,
                    MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, 0, true, false, sendMaximum,
                    MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, 0, MqttQos.EXACTLY_ONCE, true, true, true, true,
                    channel);
            handler.onSessionStartOrResume(connectionConfig, channel.eventLoop());

            final List<MqttPublishResult> acked = new ArrayList<>();
            final MqttAckFlow ackFlow = new MqttAckFlow(clientConfig) {
                @Override
                void onNext(final @NotNull MqttPublishResult result) {
                    acked.add(result);
                }

                @Override
                void acknowledged(final long acknowledged) {}
            };

            for (int i = 0; i < MqttOutgoingQosHandlerTest.PUBLISH_COUNT; i++) {
                handler.onNext(new MqttPublishWithFlow(qos0Publish("topic/" + i), ackFlow));
            }
            // run() is scheduled via execute() (immediate) and self-flushes inside run(). runPendingTasks() executes
            // those and any self-reschedule (its queue loop picks up tasks added mid-run), so it does the full drain.
            // runScheduledPendingTasks() afterwards is only a guard for a future delayed task.
            channel.runPendingTasks();
            channel.runScheduledPendingTasks();

            for (int i = 0; i < MqttOutgoingQosHandlerTest.PUBLISH_COUNT; i++) {
                final MqttStatefulPublish written = channel.readOutbound();
                assertNotNull(written, "expected a written PUBLISH but the queue drained fewer messages");
                final MqttPublish publish = written.stateless();
                // FIFO order plus unchanged message properties: the queue must pass publishes through untouched
                assertEquals("topic/" + i, publish.getTopic().toString());
                assertEquals(MqttQos.AT_MOST_ONCE, publish.getQos());
                assertArrayEquals(new byte[]{1, 2, 3}, publish.getPayloadAsBytes());
                assertFalse(publish.isRetain());
            }
            assertNull(channel.readOutbound());

            // acknowledgements are delivered for the same publishes, in the same FIFO order
            assertEquals(MqttOutgoingQosHandlerTest.PUBLISH_COUNT, acked.size());
            for (int i = 0; i < MqttOutgoingQosHandlerTest.PUBLISH_COUNT; i++) {
                assertEquals("topic/" + i, acked.get(i).getPublish().getTopic().toString());
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static @NotNull MqttPublish qos0Publish(final @NotNull String topic) {
        return new MqttPublish(
                MqttTopicImpl.of(topic), ByteBuffer.wrap(new byte[]{1, 2, 3}), MqttQos.AT_MOST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
    }
}
