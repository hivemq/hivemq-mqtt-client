package org.mqttbee.mqtt5.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import org.mqttbee.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class NettyThreadLocals {

    private static final List<ThreadLocal<?>> threadLocalMessageEncoders = new LinkedList<>();

    public static void register(@NotNull final ThreadLocal<?> threadLocalMessageEncoder) {
        threadLocalMessageEncoders.add(threadLocalMessageEncoder);
    }

    private static void clear() {
        for (final ThreadLocal<?> threadLocalMessageEncoder : threadLocalMessageEncoders) {
            threadLocalMessageEncoder.remove();
        }
    }

    public static void clear(@NotNull final EventLoopGroup eventLoopGroup) {
        for (final EventExecutor eventExecutor : eventLoopGroup) {
            eventExecutor.execute(NettyThreadLocals::clear);
        }
    }

}
