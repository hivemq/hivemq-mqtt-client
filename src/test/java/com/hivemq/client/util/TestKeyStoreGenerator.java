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

package com.hivemq.client.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author Christoph Schäbel
 */
public class TestKeyStoreGenerator {

    private static final String KEY_ALIAS = "hivemq-mqtt-client-key";

    public TestKeyStoreGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public File generateKeyStore(
            final String name,
            final String keystoreType,
            final String keyStorePassword,
            final String privateKeyPassword) throws Exception {

        final KeyStore ks = KeyStore.getInstance(keystoreType);
        ks.load(null);

        final KeyPair keyPair = generateKeyPair();
        final X509Certificate certificate = generateX509Certificate(keyPair, name);

        final X509Certificate[] certificateChain = {certificate};

        ks.setKeyEntry(KEY_ALIAS, keyPair.getPrivate(), privateKeyPassword.toCharArray(), certificateChain);

        final File keyStoreFile = File.createTempFile(name, null);
        keyStoreFile.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(keyStoreFile);
        ks.store(fos, keyStorePassword.toCharArray());
        fos.close();
        return keyStoreFile;
    }

    private X509Certificate generateX509Certificate(final KeyPair keyPair, final String name) throws Exception {

        //At least 1 attribute is required
        final X500Name x500Name = new X500Name("CN=" + name);

        final X509v3CertificateBuilder builder = new X509v3CertificateBuilder(

                x500Name, BigInteger.valueOf(new SecureRandom().nextLong()),
                new Date(System.currentTimeMillis() - 10000), new Date(System.currentTimeMillis() + 24L * 3600 * 1000),
                x500Name, SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

        final X509CertificateHolder holder = builder.build(createContentSigner(keyPair));
        final org.bouncycastle.asn1.x509.Certificate certificate = holder.toASN1Structure();

        final InputStream is = new ByteArrayInputStream(certificate.getEncoded());

        final X509Certificate x509Certificate =
                (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
        is.close();
        return x509Certificate;
    }

    private KeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private ContentSigner createContentSigner(final KeyPair keyPair) throws Exception {
        final AlgorithmIdentifier signatureAlgorithmId =
                new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        final AlgorithmIdentifier digestAlgorithmId =
                new DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgorithmId);

        final byte[] encoded = keyPair.getPrivate().getEncoded();
        final AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(encoded);

        return new BcRSAContentSignerBuilder(signatureAlgorithmId, digestAlgorithmId).build(privateKey);
    }

}