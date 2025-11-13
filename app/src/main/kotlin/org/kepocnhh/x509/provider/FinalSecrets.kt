package org.kepocnhh.x509.provider

import java.security.KeyFactory
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.cert.Certificate

internal class FinalSecrets : Secrets {
    override fun toKeyStore(
        encoded: ByteArray,
        password: CharArray,
    ): KeyStore {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(encoded.inputStream(), password)
        return keyStore
    }

    override fun toPublicKey(encoded: ByteArray): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec)
    }

    override fun toPrivateKey(encoded: ByteArray): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }

    override fun toCertificate(encoded: ByteArray): Certificate {
        val factory = CertificateFactory.getInstance("X509")
        return factory.generateCertificate(encoded.inputStream())
    }

    override fun sha256(encoded: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA256")
        return md.digest(encoded)
    }
}
