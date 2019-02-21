/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.util;

import com.hivemq.client.internal.util.Checks;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Util for creating {@link TrustManagerFactory TrustManagerFactories} and {@link KeyManagerFactory
 * KeyManagerFactories}.
 *
 * @author Christoph Schäbel
 */
public class KeyStoreUtil {

    private static final @NotNull String KEYSTORE_TYPE = "JKS";

    public static @NotNull TrustManagerFactory trustManagerFromKeystore(
            final @NotNull File trustStoreFile, final @NotNull String trustStorePassword) throws SSLException {

        Checks.notNull(trustStoreFile, "Truststore file");
        try (final FileInputStream fileInputStream = new FileInputStream(trustStoreFile)) {
            final KeyStore keyStoreTrust = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStoreTrust.load(fileInputStream, trustStorePassword.toCharArray());

            final TrustManagerFactory tmFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(keyStoreTrust);
            return tmFactory;

        } catch (final KeyStoreException | IOException e2) {
            throw new SSLException("Not able to open or read TrustStore '" + trustStoreFile.getAbsolutePath(), e2);
        } catch (final NoSuchAlgorithmException | CertificateException e3) {
            throw new SSLException(
                    "Not able to read certificate from TrustStore '" + trustStoreFile.getAbsolutePath(), e3);
        }
    }

    public static @NotNull KeyManagerFactory keyManagerFromKeystore(
            final @NotNull File keyStoreFile, final @NotNull String keyStorePassword,
            final @NotNull String privateKeyPassword) throws SSLException {

        Checks.notNull(keyStoreFile, "Keystore file");
        try (final FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, privateKeyPassword.toCharArray());
            return kmf;

        } catch (final UnrecoverableKeyException e1) {
            throw new SSLException(
                    "Not able to recover key from KeyStore, please check your private-key-password and your keyStorePassword",
                    e1);
        } catch (final KeyStoreException | IOException e2) {
            throw new SSLException("Not able to open or read KeyStore '" + keyStoreFile.getAbsolutePath(), e2);

        } catch (final NoSuchAlgorithmException | CertificateException e3) {
            throw new SSLException("Not able to read certificate from KeyStore '" + keyStoreFile.getAbsolutePath(), e3);
        }
    }
}
