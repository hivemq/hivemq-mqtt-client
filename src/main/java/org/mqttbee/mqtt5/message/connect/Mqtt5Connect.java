package org.mqttbee.mqtt5.message.connect;

import org.mqttbee.api.mqtt5.message.Mqtt5ConnectBuilder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.security.Mqtt5Authentication;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Connect implements Mqtt5Message {

    public static Mqtt5ConnectBuilder builder() {
        return new Mqtt5ConnectBuilder();
    }

    private final String clientIdentifier;
    private final int keepAlive;
    private final boolean isCleanStart;
    private final int sessionExpiryInterval;
    private final Mqtt5WillPublish willPublish;
    private final MqttConnectAuthConfiguration authConfiguration;
    private final MqttConnectRestrictionConfiguration restrictionConfiguration;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;

    Mqtt5Connect(final String clientIdentifier, final int keepAlive, final boolean isCleanStart,
                 final int sessionExpiryInterval, final Mqtt5WillPublish willPublish,
                 final MqttConnectAuthConfiguration authConfiguration,
                 final MqttConnectRestrictionConfiguration restrictionConfiguration,
                 final boolean isResponseInformationRequested, final boolean isProblemInformationRequested) {
        this.clientIdentifier = clientIdentifier;
        this.keepAlive = keepAlive;
        this.isCleanStart = isCleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.willPublish = willPublish;
        this.authConfiguration = authConfiguration;
        this.restrictionConfiguration = restrictionConfiguration;
        this.isResponseInformationRequested = isResponseInformationRequested;
        this.isProblemInformationRequested = isProblemInformationRequested;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isCleanStart() {
        return isCleanStart;
    }

    public int getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public Mqtt5WillPublish getWillPublish() {
        return willPublish;
    }

    public MqttConnectAuthConfiguration getAuthConfiguration() {
        return authConfiguration;
    }

    public MqttConnectRestrictionConfiguration getRestrictionConfiguration() {
        return restrictionConfiguration;
    }

    public boolean isResponseInformationRequested() {
        return isResponseInformationRequested;
    }

    public boolean isProblemInformationRequested() {
        return isProblemInformationRequested;
    }

    public static class MqttConnectAuthConfiguration {

        private final String username;
        private final String password;
        private final Mqtt5Authentication authentication;

        MqttConnectAuthConfiguration(final String username, final String password,
                                     final Mqtt5Authentication authentication) {
            this.username = username;
            this.password = password;
            this.authentication = authentication;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public Mqtt5Authentication getAuthentication() {
            return authentication;
        }

    }

    public static class MqttConnectRestrictionConfiguration {

        private final int receiveMaximum;
        private final int maximumPacketSize;
        private final int topicAliasMaximum;

        MqttConnectRestrictionConfiguration(final int receiveMaximum, final int maximumPacketSize,
                                            final int topicAliasMaximum) {
            this.receiveMaximum = receiveMaximum;
            this.maximumPacketSize = maximumPacketSize;
            this.topicAliasMaximum = topicAliasMaximum;
        }

        public int getReceiveMaximum() {
            return receiveMaximum;
        }

        public int getMaximumPacketSize() {
            return maximumPacketSize;
        }

        public int getTopicAliasMaximum() {
            return topicAliasMaximum;
        }

    }

}
