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

package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class FluentBuilder<B, P> {

    @NotNull
    protected static <B, P> P done(@NotNull final B built, @Nullable final Function<B, P> parentConsumer) {
        if (parentConsumer == null) {
            throw new IllegalStateException("done must not be called on the root of a fluent builder");
        }
        return parentConsumer.apply(built);
    }

    private final Function<B, P> parentConsumer;

    protected FluentBuilder(@Nullable final Function<B, P> parentConsumer) {
        this.parentConsumer = parentConsumer;
    }

    @NotNull
    public P done() {
        return done(build(), parentConsumer);
    }

    @NotNull
    public abstract B build();

}
