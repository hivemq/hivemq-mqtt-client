package org.mqttbee.mqtt5.handler.auth;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Event that is fired when the user triggers reauth.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ReAuthEvent {

    @Inject
    Mqtt5ReAuthEvent() {
    }

}
