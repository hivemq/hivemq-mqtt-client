package org.mqttbee.mqtt5.handler.auth;

import io.reactivex.CompletableEmitter;
import org.mqttbee.annotations.NotNull;

/**
 * Event that is fired when the user triggers reauth.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ReAuthEvent {

    private final CompletableEmitter reAuthEmitter;

    public Mqtt5ReAuthEvent(@NotNull final CompletableEmitter reAuthEmitter) {
        this.reAuthEmitter = reAuthEmitter;
    }

    @NotNull
    public CompletableEmitter getReAuthEmitter() {
        return reAuthEmitter;
    }

}
