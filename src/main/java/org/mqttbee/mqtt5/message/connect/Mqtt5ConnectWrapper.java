package org.mqttbee.mqtt5.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectWrapper
        extends Mqtt5WrappedMessage.Mqtt5MessageWrapper<Mqtt5ConnectWrapper, Mqtt5ConnectImpl> {

    private final Mqtt5ClientIdentifierImpl clientIdentifier;
    private final Mqtt5ExtendedAuthImpl extendedAuth;

    Mqtt5ConnectWrapper(
            @NotNull final Mqtt5ConnectImpl wrapped, @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier,
            @Nullable final Mqtt5ExtendedAuthImpl extendedAuth) {

        super(wrapped);
        this.clientIdentifier = clientIdentifier;
        this.extendedAuth = extendedAuth;
    }

    @NotNull
    public Mqtt5ClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    @Nullable
    public Mqtt5ExtendedAuthImpl getExtendedAuth() {
        return extendedAuth;
    }

    @Override
    protected Mqtt5ConnectWrapper getCodable() {
        return this;
    }

}
