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

import io.reactivex.functions.Function;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class Mqtt3ExceptionFactory {

    @NotNull
    public static Function<Throwable, Throwable> MAPPER = Mqtt3ExceptionFactory::map;

    @NotNull
    public static Throwable map(@NotNull final Throwable throwable) {
        if (throwable instanceof Mqtt5MessageException) {
            return new Mqtt3MessageException((Mqtt5MessageException) throwable);
        }
        return throwable;
    }

}
