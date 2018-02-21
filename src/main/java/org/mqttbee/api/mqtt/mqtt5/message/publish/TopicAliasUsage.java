package org.mqttbee.api.mqtt.mqtt5.message.publish;

/**
 * The handling for using a topic alias.
 *
 * @author Silvio Giebl
 */
public enum TopicAliasUsage {

    /**
     * Indicates that an outgoing PUBLISH packet must not use a topic alias.
     */
    MUST_NOT,
    /**
     * Indicates that an outgoing PUBLISH packet may use a topic alias.
     */
    MAY,
    /**
     * Indicates that an outgoing PUBLISH packet may use a topic alias and also may overwrite an existing topic alias
     * mapping.
     */
    MAY_OVERWRITE,
    /**
     * Indicates that an incoming PUBLISH packet does not have a topic alias.
     */
    HAS_NOT,
    /**
     * Indicates that an incoming PUBLISH packet has a topic alias.
     */
    HAS

}
