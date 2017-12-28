package org.mqttbee.mqtt5.security;

import org.mqttbee.mqtt5.message.auth.Mqtt5Auth;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Authentication {

    String getName();

    CompletableFuture<ByteBuffer> onConnect(final String clientIdentifier);

    CompletableFuture<Boolean> onConnAck(final String clientIdentifier, ByteBuffer authenticationData);

    CompletableFuture<Mqtt5Auth> onReauthenticate(final String clientIdentifier);

    CompletableFuture<Boolean> onReauthenticated(final String clientIdentifier, Mqtt5Auth mqttAuth);

    CompletableFuture<Mqtt5Auth> onContinue(final String clientIdentifier, Mqtt5Auth mqttAuth);

}
