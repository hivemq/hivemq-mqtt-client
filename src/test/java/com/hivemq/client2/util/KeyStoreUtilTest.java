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

import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christoph Schäbel
 */
class KeyStoreUtilTest {

    @Test
    public void test_valid_kmf() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        final KeyManagerFactory kmf = KeyStoreUtil.keyManagerFromKeystore(store, "pw", "pk");
        assertNotNull(kmf.getKeyManagers());
        assertEquals(1, kmf.getKeyManagers().length);
    }

    @Test
    public void test_wrong_kmf_ks_path() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        assertThrows(
                SSLException.class,
                () -> KeyStoreUtil.keyManagerFromKeystore(new File(store.getAbsolutePath() + "wrong"), "pw", "pk"));
    }

    @Test
    public void test_wrong_kmf_ks_pw() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        assertThrows(SSLException.class, () -> KeyStoreUtil.keyManagerFromKeystore(store, "wrong", "pk"));
    }

    @Test
    public void test_wrong_kmf_key_pw() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        assertThrows(SSLException.class, () -> KeyStoreUtil.keyManagerFromKeystore(store, "pw", "wrong"));
    }

    @Test
    public void test_valid_tmf() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        final TrustManagerFactory tmf = KeyStoreUtil.trustManagerFromKeystore(store, "pw");
        assertNotNull(tmf.getTrustManagers());
        assertEquals(1, tmf.getTrustManagers().length);
    }

    @Test
    public void test_wrong_tmf_ks_path() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        assertThrows(
                SSLException.class,
                () -> KeyStoreUtil.trustManagerFromKeystore(new File(store.getAbsolutePath() + "wrong"), "pw"));
    }

    @Test
    public void test_wrong_tmf_ks_pw() throws Exception {
        final File store = new TestKeyStoreGenerator().generateKeyStore("fun", "JKS", "pw", "pk");
        assertThrows(SSLException.class, () -> KeyStoreUtil.trustManagerFromKeystore(store, "wrong"));
    }

}