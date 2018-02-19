package org.mqttbee.mqtt5.ioc;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt3Disconnecter;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5Disconnecter;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnecter;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;

/**
 * @author Silvio Giebl
 */
@Module
public class ChannelModule {

    private final MqttClientDataImpl clientData;

    ChannelModule(@NotNull final MqttClientDataImpl clientData) {
        this.clientData = clientData;
    }

    @Provides
    MqttClientDataImpl provideClientData() {
        return clientData;
    }

    @Provides
    @ChannelScope
    MqttDisconnecter provideDisconnecter(
            final Lazy<Mqtt5Disconnecter> mqtt5Disconnecter, final Lazy<Mqtt3Disconnecter> mqtt3Disconnecter) {

        switch (clientData.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5Disconnecter.get();
            case MQTT_3_1_1:
                return mqtt3Disconnecter.get();
            default:
                throw new IllegalStateException();
        }
    }

    @Provides
    @ChannelScope
    static Ranges providePacketIdentifiers() {
        return new Ranges(UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
    }

}
