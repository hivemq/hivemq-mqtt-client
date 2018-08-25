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

package org.mqttbee.api.mqtt;

/**
 * Type for a global asynchronous stream (flow) of Publish messages. Global means not filtering on the subscriptions of
 * a Subscribe message.
 *
 * @author Silvio Giebl
 */
public enum MqttGlobalPublishFlowType {

    /**
     * Type for a global flow emitting all incoming Publish messages.
     */
    ALL_PUBLISHES,

    /**
     * Type for a global flow emitting all incoming Publish messages which match existing subscriptions.
     */
    ALL_SUBSCRIPTIONS,

    /**
     * Type for a global flow emitting all incoming Publish messages that are not emitted in per subscription or global
     * {@link #ALL_SUBSCRIPTIONS} flows.
     * <p>
     * Example (pseudo-code):
     * <ul>
     * <li><code>stream1 = client.subscribeWithStream("a/#")</code></li>
     * <li><code>client.subscribe("b/#")</code></li>
     * <li><code>stream2 = client.publishes(REMAINING_PUBLISHES)</code></li>
     * <li>=&gt; incoming Publish messages with topic <code>"a/b"</code> will be emitted only in
     * <code>stream1</code>.</li>
     * <li>=&gt; incoming Publish messages with topic <code>"b/c"</code> or <code>"c/d"</code> will be emitted in
     * <code>stream2</code> as there is no other stream registered for the topic.</li>
     * </ul>
     */
    REMAINING_PUBLISHES

}
