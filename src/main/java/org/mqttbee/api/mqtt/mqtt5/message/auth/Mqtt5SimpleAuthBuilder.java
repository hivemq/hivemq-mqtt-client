package org.mqttbee.api.mqtt.mqtt5.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SimpleAuthBuilder {

    private MqttUTF8StringImpl username;
    private ByteBuffer password;

    Mqtt5SimpleAuthBuilder() {
    }

    @NotNull
    public Mqtt5SimpleAuthBuilder withUsername(@Nullable final String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return this;
    }

    @NotNull
    public Mqtt5SimpleAuthBuilder withUsername(@Nullable final MqttUTF8String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return this;
    }

    @NotNull
    public Mqtt5SimpleAuthBuilder withPassword(@Nullable final byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @NotNull
    public Mqtt5SimpleAuthBuilder withPassword(@Nullable final ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @NotNull
    public Mqtt5SimpleAuth build() {
        Preconditions.checkState(username != null || password != null);
        return new MqttSimpleAuth(username, password);
    }

}
