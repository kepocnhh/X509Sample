package org.kepocnhh.x509.provider

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Provider
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

internal class FinalSecrets(
    private val provider: Provider,
) : Secrets {
    init {
//        error("version: ${provider.version}")
        Security.removeProvider(provider.name)
        Security.addProvider(provider)
//        error("version: ${Security.getProvider(provider.name)?.version}")
//        val providers = Security.getProviders()
//        val message = providers.joinToString(separator = "\n")
        val algorithms = Security.getAlgorithms("Signature")
        val message = algorithms.joinToString(separator = "\n")
//        error(message)
    }

    override fun newKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC", provider)
        // 192 secp192r1
        // 224 secp224r1
        // 256 secp256r1 (P-256)
        // 384 secp384r1 (P-384)
        // 521 secp521r1 (P-521)
        kpg.initialize(521)
        return kpg.generateKeyPair()
    }

    override fun csr(keyPair: KeyPair): PKCS10CertificationRequest {
        val subject = X500Principal("CN=foo bar baz") // todo
        val builder = JcaPKCS10CertificationRequestBuilder(subject, keyPair.public)
        val signer = JcaContentSignerBuilder("SHA256WITHECDSA")
            .build(keyPair.private)
        return builder.build(signer)
    }

    override fun certificate(request: PKCS10CertificationRequest, key: PrivateKey): Certificate {
        val now = System.currentTimeMillis().milliseconds
        val builder = X509v3CertificateBuilder(
            X500Name("CN=issuer"),
            BigInteger("1"),
            Date(now.inWholeMilliseconds),
            Date((now + 3650.days).inWholeMilliseconds),
            request.subject,
            request.subjectPublicKeyInfo,
        )
        val signer = JcaContentSignerBuilder("SHA256WITHECDSA")
            .build(key)
        val holder = builder.build(signer)
        val factory = CertificateFactory.getInstance("X509", provider)
        return ByteArrayInputStream(holder.toASN1Structure().encoded).use(factory::generateCertificate)
    }

    override fun toPrivateKey(encoded: ByteArray): PrivateKey {
        val keyFactory = KeyFactory.getInstance("EC", provider)
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }

    override fun toCSR(encoded: ByteArray): PKCS10CertificationRequest {
        return PKCS10CertificationRequest(encoded)
    }

    override fun toCertificate(encoded: ByteArray): Certificate {
        val factory = CertificateFactory.getInstance("X509", provider)
        return ByteArrayInputStream(encoded).use(factory::generateCertificate)
    }

    override fun sha256(encoded: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA256", provider)
        return md.digest(encoded)
    }

    override fun encrypt(key: PublicKey, decrypted: ByteArray): ByteArray {
//        val transformation = "RSA/ECB/PKCS1Padding"
        val transformation = TODO("EC")
        val cipher = Cipher.getInstance(transformation, provider)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(decrypted)
    }

    override fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray {
//        val transformation = "RSA/ECB/PKCS1Padding"
        val transformation = TODO("EC")
        val cipher = Cipher.getInstance(transformation, provider)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(encrypted)
    }

    override fun sign(key: PrivateKey, encoded: ByteArray): ByteArray {
//        val algorithm = "SHA256withRSA"
        val algorithm = TODO("EC")
        val sig = Signature.getInstance(algorithm, provider)
        sig.initSign(key)
        sig.update(encoded)
        return sig.sign()
    }

    override fun verify(key: PublicKey, encoded: ByteArray, signature: ByteArray): Boolean {
//        val algorithm = "SHA256withRSA"
        val algorithm = TODO("EC")
        val sig = Signature.getInstance(algorithm, provider)
        sig.initVerify(key)
        sig.update(encoded)
        return sig.verify(signature)
    }
}
