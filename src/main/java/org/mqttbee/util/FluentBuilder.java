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
 * Base class for builders that implement a fluent builder API.
 * <p>
 * A fluent builder knows its parent builder method that consumes the object the builder creates. Calling {@link
 * #done()} creates the builder's object and hands it over to its parent builder method.
 *
 * @param <B> the type of the object the builder creates when {@link #build()} is called
 * @param <P> the type of the parent builder
 * @author Silvio Giebl
 */
public abstract class FluentBuilder<B, P> {

    protected final Function<? super B, P> parentConsumer;

    protected FluentBuilder(@Nullable final Function<? super B, P> parentConsumer) {
        this.parentConsumer = parentConsumer;
    }

    /**
     * Creates the builder's object and hands it over to its parent builder method.
     * <p>
     * This method must not be called on the root of a fluent builder. Consider calling {@link #build()} instead.
     *
     * @return the parent builder.
     */
    @NotNull
    public P done() {
        if (parentConsumer == null) {
            throw new IllegalStateException(
                    "done must not be called on the root of a fluent builder, consider calling build() instead");
        }
        return parentConsumer.apply(build());
    }

    /**
     * Creates this builder's object.
     *
     * @return the created object.
     */
    @NotNull
    public abstract B build();

}
