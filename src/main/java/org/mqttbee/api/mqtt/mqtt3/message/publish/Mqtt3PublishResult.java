package org.mqttbee.api.mqtt.mqtt3.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3PublishResult {

    @NotNull
    Mqtt3Publish getPublish();

    boolean isSuccess();

    @Nullable
    Throwable getError();

}
