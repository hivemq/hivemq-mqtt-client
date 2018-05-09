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
 */
package org.mqttbee.mqtt.handler.ssl;

import dagger.internal.Preconditions;

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
 * @author Christoph Schäbel
 */
public class KeystoreUtil {

    private static final String KEYSTORE_TYPE = "JKS";

    public static TrustManagerFactory trustManagerFromKeystore(
            final File trustStoreFile, final String trustStorePassword) throws SSLException {

        Preconditions.checkNotNull(trustStoreFile, "Truststore must not be null");
        try (final FileInputStream fileInputStream = new FileInputStream(trustStoreFile)) {
            //load keystore from TLS config
            final KeyStore keyStoreTrust = KeyStore.getInstance("JKS");
            keyStoreTrust.load(fileInputStream, trustStorePassword.toCharArray());

            //set up TrustManagerFactory
            final TrustManagerFactory tmFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(keyStoreTrust);
            return tmFactory;

        } catch (KeyStoreException | IOException e2) {
            throw new SSLException("Not able to open or read TrustStore '" + trustStoreFile.getAbsolutePath(), e2);
        } catch (NoSuchAlgorithmException | CertificateException e3) {
            throw new SSLException(
                    "Not able to read certificate from TrustStore '" + trustStoreFile.getAbsolutePath(), e3);
        }
    }

    public static KeyManagerFactory keyManagerFromKeystore(
            final File keyStoreFile, final String keyStorePassword, final String privateKeyPassword)
            throws SSLException {

        Preconditions.checkNotNull(keyStoreFile, "Keystore must not be null");
        try (final FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            //load keystore from TLS config
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());

            //set up KeyManagerFactory with private-key-password from TLS config
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, privateKeyPassword.toCharArray());
            return kmf;

        } catch (final UnrecoverableKeyException e1) {
            throw new SSLException(
                    "Not able to recover key from KeyStore, please check your private-key-password and your keyStorePassword",
                    e1);
        } catch (KeyStoreException | IOException e2) {
            throw new SSLException("Not able to open or read KeyStore '" + keyStoreFile.getAbsolutePath(), e2);

        } catch (NoSuchAlgorithmException | CertificateException e3) {
            throw new SSLException("Not able to read certificate from KeyStore '" + keyStoreFile.getAbsolutePath(), e3);
        }
    }

}
