package org.mqttbee.api.mqtt5.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for providers for the enhanced authentication/authorization according to the MQTT 5 specification.
 * <p>
 * If an implementation stores state, an object of the implementation can not be shared by different clients. If no
 * state is stored, it has to be thread safe that it can be shared.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5EnhancedAuthProvider {

    /**
     * @return the method of this enhanced authentication/authorization provider. This method must always return the
     * same string.
     */
    @NotNull
    Mqtt5UTF8String getMethod();

    /**
     * Called when a client connects using this enhanced authentication/authorization provider.
     *
     * @param clientData  the data of the client.
     * @param connect     the Connect message.
     * @param authBuilder the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding when the required data for authentication/authorization is added
     * to the builder.
     */
    @NotNull
    CompletableFuture<Void> onAuth(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Connect connect,
            @NotNull Mqtt5EnhancedAuthBuilder authBuilder);

    /**
     * Called when a client reauthenticates and used this enhanced authentication/authorization provider during
     * connection.
     *
     * @param clientData  the data of the client.
     * @param authBuilder the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding when the required data for authentication/authorization is added
     * to the builder.
     */
    @NotNull
    CompletableFuture<Void> onReAuth(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5AuthBuilder authBuilder);

    /**
     * Called when a server reauthenticates a client and the client used this enhanced authentication/authorization
     * provider during connection. This is an addition to the MQTT 5 specification and so defaults to {@link
     * #onReAuth(Mqtt5ClientData, Mqtt5AuthBuilder)}.
     *
     * @param clientData  the data of the client.
     * @param auth        the Auth message sent by the server.
     * @param authBuilder the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the
     * authentication/authorization step and when the required data for authentication/authorization is added to the
     * builder.
     */
    @NotNull
    default CompletableFuture<Boolean> onServerReAuth(
            @NotNull final Mqtt5ClientData clientData, @NotNull @SuppressWarnings("unused") final Mqtt5Auth auth,
            @NotNull final Mqtt5AuthBuilder authBuilder) {

        return onReAuth(clientData, authBuilder).thenApply(aVoid -> true);
    }

    /**
     * Called when a server requires further data for authentication/authorization from a client which used this
     * enhanced authentication/authorization provider during connection.
     *
     * @param clientData  the data of the client.
     * @param auth        the Auth message sent by the server.
     * @param authBuilder the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the
     * authentication/authorization step and when the required data for authentication/authorization is added to the
     * builder.
     */
    @NotNull
    CompletableFuture<Boolean> onContinue(
            @NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder);

    /**
     * Called when a server accepted authentication/authorization of a client which used this enhanced
     * authentication/authorization provider during connection.
     *
     * @param clientData the data of the client.
     * @param connAck    the ConnAck message sent by the server.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the
     * authentication/authorization.
     */
    @NotNull
    CompletableFuture<Boolean> onAuthSuccess(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5ConnAck connAck);

    /**
     * Called when a server accepted reauthentication/reauthorization of a client which used this enhanced
     * authentication/authorization provider during connection.
     *
     * @param clientData the data of the client.
     * @param auth       the Auth message sent by the server.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the
     * reauthentication/reauthorization.
     */
    @NotNull
    CompletableFuture<Boolean> onReAuthSuccess(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Auth auth);

    /**
     * Called when a server rejected authentication/authorization of a client which used this enhanced
     * authentication/authorization provider during connection.
     *
     * @param clientData the data of the client.
     * @param connAck    the ConnAck message sent by the server.
     */
    void onAuthError(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5ConnAck connAck);

    /**
     * Called when a server rejected reauthentication/reauthorization of a client which used this enhanced
     * authentication/authorization provider during connection.
     *
     * @param clientData the data of the client.
     * @param disconnect the Disconnect message sent by the server.
     */
    void onReAuthError(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Disconnect disconnect);

}
