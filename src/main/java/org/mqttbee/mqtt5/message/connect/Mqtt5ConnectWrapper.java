package org.mqttbee.mqtt5.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectWrapper
        extends Mqtt5WrappedMessage.Mqtt5MessageWrapper<Mqtt5ConnectWrapper, Mqtt5ConnectImpl> {

    private final Mqtt5ClientIdentifierImpl clientIdentifier;
    private final Mqtt5EnhancedAuthImpl enhancedAuth;

    Mqtt5ConnectWrapper(
            @NotNull final Mqtt5ConnectImpl wrapped, @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier,
            @Nullable final Mqtt5EnhancedAuthImpl enhancedAuth) {

        super(wrapped);
        this.clientIdentifier = clientIdentifier;
        this.enhancedAuth = enhancedAuth;
    }

    @NotNull
    public Mqtt5ClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    @Nullable
    public Mqtt5EnhancedAuthImpl getEnhancedAuth() {
        return enhancedAuth;
    }

    @Override
    protected Mqtt5ConnectWrapper getCodable() {
        return this;
    }

}
