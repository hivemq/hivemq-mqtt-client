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

package com.hivemq.client2.mqtt;

/**
 * Global filter for incoming Publish messages.
 * <p>
 * Global means not filtering for individual subscriptions of a Subscribe message.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum MqttGlobalPublishFilter {

    /**
     * Filter matching all incoming Publish messages.
     * <p>
     * ALL = {@link #SUBSCRIBED} + {@link #UNSOLICITED}.
     */
    ALL,

    /**
     * Filter matching the incoming Publish messages resulting from subscriptions made by the client.
     * <p>
     * SUBSCRIBED + {@link #UNSOLICITED} = {@link #ALL}.
     */
    SUBSCRIBED,

    /**
     * Filter matching the incoming Publish messages not resulting from any subscription made by the client.
     * <p>
     * UNSOLICITED + {@link #SUBSCRIBED} = {@link #ALL}.
     */
    UNSOLICITED,

    /**
     * Filter matching the incoming Publish messages that are not consumed in per subscription or other global flows.
     * <p>
     * This filter will not match any messages if
     * <ul>
     *   <li>the {@link #ALL} filter is used or
     *   <li>both {@link #SUBSCRIBED} and {@link #UNSOLICITED} filters are used.
     * </ul>
     * <p>
     * Example (pseudo-code):
     * <ul>
     *   <li><code>stream1 = client.subscribePublishes("a/#")</code>
     *   <li><code>client.subscribe("b/#")</code>
     *   <li><code>stream2 = client.publishes(SUBSCRIBED)</code>
     *   <li><code>stream3 = client.publishes(REMAINING)</code>
     * </ul>
     * Result: incoming Publishes with topic
     * <ul>
     *   <li><code>"a/b"</code> will be emitted in <code>stream1</code> and <code>stream2</code>.
     *   <li><code>"b/c"</code> will be emitted in <code>stream2</code>
     *   <li><code>"c/d"</code> will be emitted in <code>stream3</code> as there is no other stream registered for the
     *     topic.
     * </ul>
     */
    REMAINING
}
