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

package com.hivemq.client.internal.mqtt.handler.websocket;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for WebSocket URI construction, specifically verifying that pre-encoded query strings
 * (such as AWS IoT presigned URLs) are not double-encoded.
 *
 * @see <a href="https://github.com/hivemq/hivemq-mqtt-client/issues/421">GitHub Issue #421</a>
 */
class MqttWebSocketInitializerTest {

    @Test
    void emptyQueryString_shouldProduceValidUri() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "example.com", 443, "mqtt", "");
        assertEquals("wss://example.com:443/mqtt", uri.toString());
    }

    @Test
    void nullQueryString_shouldProduceValidUri() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "example.com", 443, "mqtt", null);
        assertEquals("wss://example.com:443/mqtt", uri.toString());
    }

    @Test
    void simpleQueryString_shouldNotBeEncoded() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "example.com", 443, "mqtt", "clientId=test");
        assertEquals("wss://example.com:443/mqtt?clientId=test", uri.toString());
    }

    @Test
    void multipleQueryParams_shouldPreserveStructure() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "example.com", 443, "mqtt", "clientId=test&clean=true");
        assertEquals("wss://example.com:443/mqtt?clientId=test&clean=true", uri.toString());
    }

    @Test
    void preEncodedQueryString_shouldNotBeDoubleEncoded() throws URISyntaxException, UnsupportedEncodingException {
        // AWS IoT presigned URLs contain already-encoded characters like %2F, %3D
        // This test verifies they are NOT double-encoded (which would turn %2F into %252F)
        final String preEncodedQuery = "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA%2F20231001%2Fus-east-1%2Fiot";
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "iot.amazonaws.com", 443, "mqtt", preEncodedQuery);

        // The query string should be preserved as-is, not double-encoded
        final String uriString = uri.toString();
        assertEquals("wss://iot.amazonaws.com:443/mqtt?" + preEncodedQuery, uriString);

        // Verify %2F was NOT double-encoded to %252F
        assertFalse(uriString.contains("%252F"), "Query string was double-encoded - %2F became %252F");
    }

    @Test
    void complexAwsPresignedUrl_shouldPreserveSignature() throws URISyntaxException, UnsupportedEncodingException {
        // Realistic AWS IoT Core presigned URL query string
        final String awsPresignedQuery = "X-Amz-Algorithm=AWS4-HMAC-SHA256" +
                "&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20231201%2Fus-east-1%2Fiotdevicegateway%2Faws4_request" +
                "&X-Amz-Date=20231201T120000Z" +
                "&X-Amz-SignedHeaders=host" +
                "&X-Amz-Signature=abcd1234signature5678";

        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "iot.us-east-1.amazonaws.com", 443, "mqtt", awsPresignedQuery);

        // Verify the URI contains the query exactly as provided
        final String uriString = uri.toString();
        assertEquals("wss://iot.us-east-1.amazonaws.com:443/mqtt?" + awsPresignedQuery, uriString);
    }

    @Test
    void wsScheme_shouldWorkWithoutSsl() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("ws", "localhost", 8080, "mqtt", "test=value");
        assertEquals("ws://localhost:8080/mqtt?test=value", uri.toString());
    }

    @Test
    void pathWithSubdirectory_shouldBePreserved() throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = MqttWebSocketInitializer.buildWebSocketUri("wss", "example.com", 443, "api/v1/mqtt", "token=abc");
        assertEquals("wss://example.com:443/api/v1/mqtt?token=abc", uri.toString());
    }

    /**
     * This test demonstrates the bug that existed before the fix.
     * With the old implementation using URI(scheme, null, host, port, path, query, null),
     * the query string would be encoded, causing pre-encoded strings to be double-encoded.
     */
    @Test
    void oldBehavior_wouldDoubleEncode() throws URISyntaxException {
        // This is how the OLD implementation built the URI:
        final URI oldStyleUri = new URI("wss", null, "example.com", 443, "/mqtt",
                "credential=AKIA%2F20231001", null);

        // The old implementation would double-encode, turning %2F into %252F
        final String oldUriString = oldStyleUri.toString();
        // This assertion shows the bug: %2F becomes %252F
        assertEquals("wss://example.com:443/mqtt?credential=AKIA%252F20231001", oldUriString,
                "This demonstrates the double-encoding bug in the old implementation");
    }
}