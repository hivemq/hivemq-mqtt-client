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
 */
package org.mqttbee.mqtt.mqtt3.exceptions;

import org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;

/**
 * @author David Katz
 */
public class Mqtt3ExceptionFactory {
    public static Throwable map(Throwable throwable) {
        if (throwable instanceof Mqtt5MessageException) {
            Mqtt5MessageException mqtt5Exception = (Mqtt5MessageException) throwable;
            return new Mqtt3MessageException(mqtt5Exception);
        } else {
            return throwable;
        }
    }
}
