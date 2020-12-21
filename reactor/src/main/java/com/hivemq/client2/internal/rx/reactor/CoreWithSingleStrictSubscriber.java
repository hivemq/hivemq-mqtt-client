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

package com.hivemq.client2.internal.rx.reactor;

import com.hivemq.client2.internal.rx.WithSingleStrictSubscriber;
import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import com.hivemq.client2.rx.reactor.CoreWithSingleSubscriber;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class CoreWithSingleStrictSubscriber<F, S> extends WithSingleStrictSubscriber<F, S>
        implements CoreWithSingleSubscriber<F, S> {

    public CoreWithSingleStrictSubscriber(final @NotNull WithSingleSubscriber<F, S> subscriber) {
        super(subscriber);
    }
}
