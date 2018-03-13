package org.mqttbee.api.mqtt;

import com.google.common.base.Preconditions;
import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigBuilder {

    private Executor nettyExecutor;
    private int nettyThreads = MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS;
    private Scheduler rxJavaScheduler = MqttClientExecutorConfigImpl.DEFAULT_RX_JAVA_SCHEDULER;

    @NotNull
    public MqttClientExecutorConfigBuilder usingNettyExecutor(@NotNull final Executor nettyExecutor) {
        Preconditions.checkNotNull(nettyExecutor);
        this.nettyExecutor = nettyExecutor;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder usingNettyThreads(final int nettyThreads) {
        Preconditions.checkArgument(nettyThreads > 0);
        this.nettyThreads = nettyThreads;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder usingRxJavaScheduler(@NotNull final Scheduler rxJavaScheduler) {
        Preconditions.checkNotNull(rxJavaScheduler);
        this.rxJavaScheduler = rxJavaScheduler;
        return this;
    }

    public MqttClientExecutorConfig build() {
        return new MqttClientExecutorConfigImpl(nettyExecutor, nettyThreads, rxJavaScheduler);
    }

}
