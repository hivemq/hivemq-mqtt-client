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

package org.mqttbee.mqtt;

/**
 * Global filter for Publish messages. Global means not filtering for individual subscriptions of a Subscribe message.
 *
 * @author Silvio Giebl
 */
public enum MqttGlobalPublishFilter {

    /**
     * Filter matching all incoming Publish messages.
     */
    ALL_PUBLISHES,

    /**
     * Filter matching all subscriptions made by the client.
     */
    ALL_SUBSCRIPTIONS,

    /**
     * Filter matching all incoming Publish messages that are not consumed in per subscription or global {@link
     * #ALL_SUBSCRIPTIONS} flows.
     * <p>
     * Example (pseudo-code):
     * <ul>
     * <li><code>stream1 = client.subscribeWithStream("a/#")</code></li>
     * <li><code>client.subscribe("b/#")</code></li>
     * <li><code>stream2 = client.publishes(ALL_SUBSCRIPTIONS)</code></li>
     * <li><code>stream3 = client.publishes(REMAINING_PUBLISHES)</code></li>
     * </ul>
     * Result: incoming Publishes with topic
     * <ul>
     * <li><code>"a/b"</code> will be emitted in <code>stream1</code> and <code>stream2</code>.</li>
     * <li><code>"b/c"</code> will be emitted in <code>stream2</code></li>
     * <li><code>"c/d"</code> will be emitted in <code>stream3</code> as there is no other stream registered for the
     * topic.</li>
     * </ul>
     */
    REMAINING_PUBLISHES

}
