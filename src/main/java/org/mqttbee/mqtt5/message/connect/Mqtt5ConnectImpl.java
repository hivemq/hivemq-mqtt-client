package org.mqttbee.mqtt5.message.connect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Connect;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectImpl implements Mqtt5Connect {

    public static final int NOT_DEFAULT_RESPONSE_INFORMATION_REQUESTED = DEFAULT_RESPONSE_INFORMATION_REQUESTED ? 0 : 1;
    public static final int NOT_DEFAULT_PROBLEM_INFORMATION_REQUESTED = DEFAULT_PROBLEM_INFORMATION_REQUESTED ? 0 : 1;

    private final Mqtt5ClientIdentifier clientIdentifier;
    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final RestrictionsImpl restrictions;
    private final AuthImpl auth;
    private final Mqtt5WillPublishImpl willPublish;
    private final List<Mqtt5UserProperty> userProperties;

    public Mqtt5ConnectImpl(
            @NotNull final Mqtt5ClientIdentifier clientIdentifier, final int keepAlive, final boolean isCleanStart,
            final long sessionExpiryInterval, final boolean isResponseInformationRequested,
            final boolean isProblemInformationRequested, @NotNull final RestrictionsImpl restrictions,
            @Nullable final AuthImpl auth, @Nullable final Mqtt5WillPublishImpl willPublish,
            @NotNull final List<Mqtt5UserProperty> userProperties) {
        this.clientIdentifier = clientIdentifier;
        this.keepAlive = keepAlive;
        this.isCleanStart = isCleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.isResponseInformationRequested = isResponseInformationRequested;
        this.isProblemInformationRequested = isProblemInformationRequested;
        this.restrictions = restrictions;
        this.auth = auth;
        this.willPublish = willPublish;
        this.userProperties = Collections.unmodifiableList(userProperties);
    }

    @NotNull
    @Override
    public Mqtt5ClientIdentifier getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public boolean isCleanStart() {
        return isCleanStart;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return isResponseInformationRequested;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return isProblemInformationRequested;
    }

    @NotNull
    @Override
    public Restrictions getRestrictions() {
        return restrictions;
    }

    @NotNull
    public RestrictionsImpl getRawRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<Auth> getAuth() {
        return Optional.ofNullable(auth);
    }

    @Nullable
    public AuthImpl getRawAuth() {
        return auth;
    }

    @NotNull
    @Override
    public Optional<Mqtt5WillPublishImpl> getWillPublish() {
        return Optional.ofNullable(willPublish);
    }

    @Nullable
    public Mqtt5WillPublishImpl getRawWillPublish() {
        return willPublish;
    }

    @NotNull
    @Override
    public List<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNECT;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getConnectEncoder().encode(this, channel, out);
    }


    public static class AuthImpl implements Auth {

        @Nullable
        public static final Auth DEFAULT_NO_AUTH = null;

        private final Mqtt5UTF8String username;
        private final byte[] password;
        private final Mqtt5UTF8String method;
        private final byte[] data;

        public AuthImpl(
                @Nullable final Mqtt5UTF8String username, @Nullable final byte[] password,
                @Nullable final Mqtt5UTF8String method, @Nullable final byte[] data) {
            this.username = username;
            this.password = password;
            this.method = method;
            this.data = data;
        }

        @NotNull
        @Override
        public Optional<Mqtt5UTF8String> getUsername() {
            return Optional.ofNullable(username);
        }

        @Nullable
        public Mqtt5UTF8String getRawUsername() {
            return username;
        }

        @NotNull
        @Override
        public Optional<byte[]> getPassword() {
            return Optional.ofNullable(password);
        }

        @Nullable
        public byte[] getRawPassword() {
            return password;
        }

        @NotNull
        @Override
        public Optional<Mqtt5UTF8String> getMethod() {
            return Optional.ofNullable(method);
        }

        @Nullable
        public Mqtt5UTF8String getRawMethod() {
            return method;
        }

        @NotNull
        @Override
        public Optional<byte[]> getData() {
            return Optional.ofNullable(data);
        }

        @Nullable
        public byte[] getRawData() {
            return data;
        }

    }


    public static class RestrictionsImpl implements Restrictions {

        @NotNull
        public static final Restrictions DEFAULT = new RestrictionsImpl(
                DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);

        private final int receiveMaximum;
        private final long topicAliasMaximum;
        private final int maximumPacketSize;

        public RestrictionsImpl(
                final int receiveMaximum, final long topicAliasMaximum, final int maximumPacketSize) {
            this.receiveMaximum = receiveMaximum;
            this.topicAliasMaximum = topicAliasMaximum;
            this.maximumPacketSize = maximumPacketSize;
        }

        @Override
        public int getReceiveMaximum() {
            return receiveMaximum;
        }

        @Override
        public long getTopicAliasMaximum() {
            return topicAliasMaximum;
        }

        @Override
        public int getMaximumPacketSize() {
            return maximumPacketSize;
        }

    }

}
