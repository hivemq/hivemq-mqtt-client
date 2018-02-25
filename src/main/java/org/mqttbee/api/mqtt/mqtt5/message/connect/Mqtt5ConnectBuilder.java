package org.mqttbee.api.mqtt.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5ConnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuthImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictionsImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.*;

public class Mqtt5ConnectBuilder {

    private int keepAlive = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryInterval = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private MqttConnectRestrictionsImpl restrictions;
    private MqttSimpleAuthImpl simpleAuth;
    private Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private MqttWillPublishImpl willPublish;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5ConnectBuilder() {
    }

    Mqtt5ConnectBuilder(@NotNull final Mqtt5Connect connect) {
        final MqttConnectImpl connectImpl =
                MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnectImpl.class);
        keepAlive = connectImpl.getKeepAlive();
        isCleanStart = connectImpl.isCleanStart();
        sessionExpiryInterval = connectImpl.getSessionExpiryInterval();
        isResponseInformationRequested = connectImpl.isResponseInformationRequested();
        isProblemInformationRequested = connectImpl.isProblemInformationRequested();
        restrictions = connectImpl.getRestrictions();
        simpleAuth = connectImpl.getRawSimpleAuth();
        enhancedAuthProvider = connectImpl.getRawEnhancedAuthProvider();
        willPublish = connectImpl.getRawWillPublish();
        userProperties = connectImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5ConnectBuilder withKeepAlive(final int keepAlive) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAlive));
        this.keepAlive = keepAlive;
        return this;
    }

    public Mqtt5ConnectBuilder withCleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withResponseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withProblemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withRestrictions(@NotNull final Mqtt5ConnectRestrictions restrictions) {
        this.restrictions =
                MustNotBeImplementedUtil.checkNotImplemented(restrictions, MqttConnectRestrictionsImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSimpleAuth(@Nullable final Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, MqttSimpleAuthImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withEnhancedAuth(@Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {
        this.enhancedAuthProvider = enhancedAuthProvider;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withWillPublish(@Nullable final Mqtt5WillPublish willPublish) {
        this.willPublish = MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, MqttWillPublishImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Connect build() {
        return new MqttConnectImpl(keepAlive, isCleanStart, sessionExpiryInterval, isResponseInformationRequested,
                isProblemInformationRequested, restrictions, simpleAuth, enhancedAuthProvider, willPublish,
                userProperties, Mqtt5ConnectEncoder.PROVIDER);
    }

}