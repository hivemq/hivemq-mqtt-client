package org.mqttbee.mqtt5.ioc;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Silvio Giebl
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface ChannelScope {
}
