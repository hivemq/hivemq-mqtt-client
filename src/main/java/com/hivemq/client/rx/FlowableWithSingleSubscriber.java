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

package com.hivemq.client.rx;

import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.rxjava3.core.FlowableSubscriber;

/**
 * Represents a Reactive-Streams inspired {@link WithSingleSubscriber} that is RxJava 2 only and weakens rules for
 * gaining performance. Most important is the weakening of rule §1.3: onNext/onSingle could be called concurrently until
 * onSubscribe returns.
 *
 * @param <F> the type of the stream of items.
 * @param <S> the type of the single item.
 * @author Silvio Giebl
 * @see FlowableSubscriber
 */
public interface FlowableWithSingleSubscriber<F, S> extends WithSingleSubscriber<F, S>, FlowableSubscriber<F> {}
