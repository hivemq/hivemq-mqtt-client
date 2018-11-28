/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PublishBuilder extends Mqtt5PublishBuilderBase.Base<Mqtt5PublishBuilder.Complete> {

    @NotNull Mqtt5WillPublishBuilder asWill();

    @DoNotImplement
    interface Complete
            extends Mqtt5PublishBuilder, Mqtt5PublishBuilderBase.Base.Complete<Mqtt5PublishBuilder.Complete> {

        @NotNull Mqtt5WillPublishBuilder.Complete asWill();

        @NotNull Mqtt5Publish build();
    }

    @DoNotImplement
    interface Nested<P> extends Mqtt5PublishBuilderBase.Base<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt5PublishBuilderBase.Base.Complete<Nested.Complete<P>> {

            @NotNull P applyPublish();
        }
    }

    @DoNotImplement
    interface Send<P> extends Mqtt5PublishBuilderBase.Base<Send.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Send<P>, Mqtt5PublishBuilderBase.Base.Complete<Send.Complete<P>> {

            @NotNull P send();
        }
    }
}
