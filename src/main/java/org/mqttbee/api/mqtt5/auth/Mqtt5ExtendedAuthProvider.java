package org.mqttbee.api.mqtt5.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthBuilderImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthBuilderImpl;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ExtendedAuthProvider {

    @NotNull
    Mqtt5UTF8String getMethod();

    @NotNull
    CompletableFuture<Void> onAuthenticate(
            @NotNull Mqtt5Connect connect, @NotNull Mqtt5ExtendedAuthBuilderImpl authBuilder);

    @NotNull
    CompletableFuture<Void> onReauthenticate(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5AuthBuilderImpl authBuilder);

    @NotNull
    default CompletableFuture<Void> onServerReauthenticate(
            @NotNull final Mqtt5ClientData clientData, @NotNull @SuppressWarnings("unused") final Mqtt5Auth auth,
            @NotNull final Mqtt5AuthBuilderImpl authBuilder) {

        return onReauthenticate(clientData, authBuilder);
    }

    @NotNull
    CompletableFuture<Void> onContinue(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilderImpl authBuilder);

    @NotNull
    CompletableFuture<Boolean> onAuthenticated(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5ConnAck connAck);

    @NotNull
    CompletableFuture<Boolean> onReauthenticated(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Auth mqttAuth);

}
