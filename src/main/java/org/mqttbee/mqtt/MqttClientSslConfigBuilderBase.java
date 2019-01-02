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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttClientSslConfigBuilderBase<B extends MqttClientSslConfigBuilderBase<B>> {

    @NotNull B keyManagerFactory(@Nullable KeyManagerFactory keyManagerFactory);

    @NotNull B trustManagerFactory(@Nullable TrustManagerFactory trustManagerFactory);

    /**
     * @param cipherSuites if <code>null</code>, netty's default cipher suites will be used.
     */
    @NotNull B cipherSuites(@Nullable List<@NotNull String> cipherSuites);

    /**
     * @param protocols if <code>null</code>, netty's default protocols will be used.
     */
    @NotNull B protocols(@Nullable List<@NotNull String> protocols);

    @NotNull B handshakeTimeout(long timeout, @NotNull TimeUnit timeUnit);
}
