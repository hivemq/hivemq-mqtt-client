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

package com.hivemq.client2.internal.util;

import io.netty.util.NetUtil;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author Silvio Giebl
 */
public final class InetSocketAddressUtil {

    public static @NotNull InetSocketAddress create(final @NotNull String host, final int port) {
        final byte[] ipAddress = NetUtil.createByteArrayFromIpAddressString(host);
        if (ipAddress != null) {
            try {
                return new InetSocketAddress(InetAddress.getByAddress(ipAddress), port);
            } catch (final UnknownHostException e) {
                // ignore must not happen
            }
        }
        return InetSocketAddress.createUnresolved(host, port);
    }

    private InetSocketAddressUtil() {}
}
