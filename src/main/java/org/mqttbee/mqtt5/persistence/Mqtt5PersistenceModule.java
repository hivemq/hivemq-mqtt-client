package org.mqttbee.mqtt5.persistence;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.memory.OutgoingMemoryQoSFlowPersistence;

/**
 * @author Silvio Giebl
 */
@Module
public class Mqtt5PersistenceModule {

    @Provides
    @ChannelScope
    static OutgoingQoSFlowPersistence provideOutgoingQoSFlowPersistence(
            @NotNull final Lazy<OutgoingMemoryQoSFlowPersistence> memoryPersistence) {

        return memoryPersistence.get(); // TODO file persistence
    }

}
