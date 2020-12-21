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

package com.hivemq.client2.util;

import com.hivemq.client2.internal.util.Checks;
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

        Checks.notNull(trustStoreFile, "Trust store file");
        try (final FileInputStream fileInputStream = new FileInputStream(trustStoreFile)) {
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(fileInputStream, trustStorePassword.toCharArray());

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf;

        } catch (final KeyStoreException | IOException e) {
            throw new SSLException(
                    "Not able to open or read trust store '" + trustStoreFile.getAbsolutePath() + "'", e);
        } catch (final NoSuchAlgorithmException | CertificateException e) {
            throw new SSLException(
                    "Not able to read certificate from trust store '" + trustStoreFile.getAbsolutePath() + "'", e);
        }
    }

    public static @NotNull KeyManagerFactory keyManagerFromKeystore(
            final @NotNull File keyStoreFile,
            final @NotNull String keyStorePassword,
            final @NotNull String privateKeyPassword) throws SSLException {

        Checks.notNull(keyStoreFile, "Key store file");
        try (final FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, privateKeyPassword.toCharArray());
            return kmf;

        } catch (final UnrecoverableKeyException e) {
            throw new SSLException(
                    "Not able to recover key from key store, please check your private key password and your key store password",
                    e);
        } catch (final KeyStoreException | IOException e) {
            throw new SSLException("Not able to open or read key store '" + keyStoreFile.getAbsolutePath() + "'", e);

        } catch (final NoSuchAlgorithmException | CertificateException e) {
            throw new SSLException(
                    "Not able to read certificate from key store '" + keyStoreFile.getAbsolutePath() + "'", e);
        }
    }
}
