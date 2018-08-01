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

package org.mqttbee.mqtt.message.ping.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.ping.Mqtt3PingReq;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PingReqView implements Mqtt3PingReq {

    private static final Mqtt3PingReqView INSTANCE = new Mqtt3PingReqView();

    @NotNull
    public static Mqtt3PingReqView of() {
        return INSTANCE;
    }

    private Mqtt3PingReqView() {
    }

}
