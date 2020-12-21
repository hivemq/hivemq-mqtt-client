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

package com.hivemq.client2.rx.reactor;

import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import reactor.core.CoreSubscriber;

/**
 * A {@link reactor.util.context.Context Context} aware {@link WithSingleSubscriber} which has relaxed rules for §1.3
 * and §3.9 compared to the original {@link org.reactivestreams.Subscriber} from Reactive Streams.
 *
 * @author Silvio Giebl
 * @see WithSingleSubscriber
 * @see CoreSubscriber
 * @since 1.2
 */
public interface CoreWithSingleSubscriber<T, S> extends WithSingleSubscriber<T, S>, CoreSubscriber<T> {}
