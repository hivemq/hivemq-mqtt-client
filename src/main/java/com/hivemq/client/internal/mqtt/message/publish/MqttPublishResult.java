/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * @author Silvio Giebl
 */
public class MqttPublishResult implements Mqtt5PublishResult {

    private final @NotNull MqttPublish publish;
    private final @Nullable Throwable error;

    public MqttPublishResult(final @NotNull MqttPublish publish, final @Nullable Throwable error) {
        this.publish = publish;
        this.error = error;
    }

    @Override
    public @NotNull MqttPublish getPublish() {
        return publish;
    }

    @Override
    public @NotNull Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public @Nullable Throwable getRawError() {
        return error;
    }

    public boolean acknowledged() {
        return true;
    }

    @NotNull String toAttributeString() {
        return "publish=" + publish + ((error == null) ? "" : ", error=" + error);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttPublishResult)) {
            return false;
        }
        final MqttPublishResult that = (MqttPublishResult) o;

        return that.canEqual(this) && publish.equals(that.publish) && Objects.equals(error, that.error);
    }

    protected boolean canEqual(final @Nullable Object o) {
        return o instanceof MqttPublishResult;
    }

    @Override
    public int hashCode() {
        int result = publish.hashCode();
        result = 31 * result + Objects.hashCode(error);
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "MqttPublishResult{" + toAttributeString() + '}';
    }

    public static class MqttQos1Result extends MqttPublishResult implements Mqtt5Qos1Result {

        private final @NotNull MqttPubAck pubAck;

        public MqttQos1Result(
                final @NotNull MqttPublish publish, final @Nullable Throwable error, final @NotNull MqttPubAck pubAck) {

            super(publish, error);
            this.pubAck = pubAck;
        }

        @Override
        public @NotNull MqttPubAck getPubAck() {
            return pubAck;
        }

        @Override
        @NotNull String toAttributeString() {
            return super.toAttributeString() + ", pubAck=" + pubAck;
        }

        @Override
        public @NotNull String toString() {
            return "MqttQos1Result{" + toAttributeString() + '}';
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MqttQos1Result) || !super.equals(o)) {
                return false;
            }
            final MqttQos1Result that = (MqttQos1Result) o;

            return pubAck.equals(that.pubAck);
        }

        @Override
        protected boolean canEqual(final @Nullable Object o) {
            return o instanceof MqttQos1Result;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + pubAck.hashCode();
            return result;
        }
    }

    public static class MqttQos2Result extends MqttPublishResult implements Mqtt5Qos2Result {

        private final @NotNull MqttPubRec pubRec;

        public MqttQos2Result(
                final @NotNull MqttPublish publish, final @Nullable Throwable error, final @NotNull MqttPubRec pubRec) {

            super(publish, error);
            this.pubRec = pubRec;
        }

        @Override
        public @NotNull MqttPubRec getPubRec() {
            return pubRec;
        }

        @Override
        @NotNull String toAttributeString() {
            return super.toAttributeString() + ", pubRec=" + pubRec;
        }

        @Override
        public @NotNull String toString() {
            return "MqttQos2Result{" + toAttributeString() + '}';
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MqttQos2Result) || !super.equals(o)) {
                return false;
            }
            final MqttQos2Result that = (MqttQos2Result) o;

            return pubRec.equals(that.pubRec);
        }

        @Override
        protected boolean canEqual(final @Nullable Object o) {
            return o instanceof MqttQos2Result;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + pubRec.hashCode();
            return result;
        }
    }

    public static class MqttQos2IntermediateResult extends MqttQos2Result {

        private final @NotNull BooleanSupplier acknowledgedCallback;

        public MqttQos2IntermediateResult(
                final @NotNull MqttPublish publish,
                final @NotNull MqttPubRec pubRec,
                final @NotNull BooleanSupplier acknowledgedCallback) {

            super(publish, null, pubRec);
            this.acknowledgedCallback = acknowledgedCallback;
        }

        @Override
        public boolean acknowledged() {
            return acknowledgedCallback.getAsBoolean();
        }
    }

    public static class MqttQos2CompleteResult extends MqttQos2Result implements Mqtt5Qos2CompleteResult {

        private final @NotNull MqttPubRel pubRel;
        private final @NotNull MqttPubComp pubComp;

        public MqttQos2CompleteResult(
                final @NotNull MqttPublish publish,
                final @NotNull MqttPubRec pubRec,
                final @NotNull MqttPubRel pubRel,
                final @NotNull MqttPubComp pubComp) {

            super(publish, null, pubRec);
            this.pubRel = pubRel;
            this.pubComp = pubComp;
        }

        @Override
        public @NotNull MqttPubRel getPubRel() {
            return pubRel;
        }

        @Override
        public @NotNull MqttPubComp getPubComp() {
            return pubComp;
        }

        @Override
        @NotNull String toAttributeString() {
            return super.toAttributeString() + ", pubRel=" + pubRel + ", pubComp=" + pubComp;
        }

        @Override
        public @NotNull String toString() {
            return "MqttQos2CompleteResult{" + toAttributeString() + '}';
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MqttQos2CompleteResult) || !super.equals(o)) {
                return false;
            }
            final MqttQos2CompleteResult that = (MqttQos2CompleteResult) o;

            return pubRel.equals(that.pubRel) && pubComp.equals(that.pubComp);
        }

        @Override
        protected boolean canEqual(final @Nullable Object o) {
            return o instanceof MqttQos2CompleteResult;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + pubRel.hashCode();
            result = 31 * result + pubComp.hashCode();
            return result;
        }
    }
}
