package com.hivemq.client.restrictions;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.client.ClientContext;
import com.hivemq.extension.sdk.api.client.parameter.InitializerInput;
import com.hivemq.extension.sdk.api.interceptor.puback.PubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.puback.parameter.PubackOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.puback.parameter.PubackOutboundOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Yannick Weber
 */
public class Mqtt3SendMaximumIT {

    public static final int RECEIVE_MAXIMUM = 10;
    public static final @NotNull HiveMQExtension NO_PUBACK_EXTENSION = HiveMQExtension.builder()
            .version("1.0.0")
            .priority(100)
            .name("No PUBACK Extension")
            .id("no-puback-extension")
            .mainClass(NoPubackExtension.class)
            .build();

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension().withExtension(NO_PUBACK_EXTENSION)
                    .withHiveMQConfig(MountableFile.forClasspathResource("/config.xml"));

    @Test
    void mqtt3_sendMaximum_applied() throws InterruptedException {

        final Mqtt3Client publisher = Mqtt3Client.builder().serverPort(hivemq.getMqttPort()).build();
        publisher.toBlocking().connectWith().restrictions().sendMaximum(RECEIVE_MAXIMUM).applyRestrictions().send();

        final ConcurrentLinkedQueue<Mqtt5Publish> publishes = new ConcurrentLinkedQueue<>();
        final Mqtt5BlockingClient subscriber = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();
        subscriber.connect();
        subscriber.toAsync().publishes(MqttGlobalPublishFilter.ALL, publishes::add);
        subscriber.subscribeWith().topicFilter("#").send();

        for (int i = 0; i < 12; i++) {
            publisher.toAsync().publishWith().topic("test").qos(MqttQos.AT_LEAST_ONCE).send();
        }

        await().until(() -> publishes.size() == RECEIVE_MAXIMUM);

        TimeUnit.SECONDS.sleep(2);

        assertEquals(RECEIVE_MAXIMUM, publishes.size());
    }

    public static class NoPubackExtension implements ExtensionMain {

        @Override
        public void extensionStart(
                final @NotNull ExtensionStartInput extensionStartInput,
                final @NotNull ExtensionStartOutput extensionStartOutput) {
            Services.initializerRegistry().setClientInitializer(new MyClientInitializer());
        }

        @Override
        public void extensionStop(
                final @NotNull ExtensionStopInput extensionStopInput,
                final @NotNull ExtensionStopOutput extensionStopOutput) {

        }
    }

    public static class MyClientInitializer implements ClientInitializer {

        @Override
        public void initialize(
                final @NotNull InitializerInput initializerInput, final @NotNull ClientContext clientContext) {
            clientContext.addPubackOutboundInterceptor(new NoPubackInterceptorHandler());
        }
    }

    public static class NoPubackInterceptorHandler implements PubackOutboundInterceptor {

        @Override
        public void onOutboundPuback(
                final @NotNull PubackOutboundInput pubackOutboundInput,
                final @NotNull PubackOutboundOutput pubackOutboundOutput) {
            pubackOutboundOutput.async(Duration.ofHours(1));
        }
    }

}
