package org.mqttbee.api.mqtt;

import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public interface MqttClientExecutorConfig {

    @NotNull
    static MqttClientExecutorConfigBuilder builder() {
        return new MqttClientExecutorConfigBuilder();
    }

    @NotNull
    Optional<Executor> getUserDefinedNettyExecutor();

    @NotNull
    Optional<Integer> getUserDefinedNettyThreads();

    @NotNull
    Scheduler getRxJavaScheduler();

}
