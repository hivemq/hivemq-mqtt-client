package org.mqttbee.mqtt5.security;

import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Authentication {

    String getName();

    CompletableFuture<ByteBuffer> onConnect(final String clientIdentifier);

    CompletableFuture<Boolean> onConnAck(final String clientIdentifier, ByteBuffer authenticationData);

    CompletableFuture<Mqtt5AuthImpl> onReauthenticate(final String clientIdentifier);

    CompletableFuture<Boolean> onReauthenticated(final String clientIdentifier, Mqtt5AuthImpl mqttAuth);

    CompletableFuture<Mqtt5AuthImpl> onContinue(final String clientIdentifier, Mqtt5AuthImpl mqttAuth);

}
