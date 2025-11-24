package org.kepocnhh.x509.provider

import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate

internal interface Secrets {
    fun newKeyPair(): KeyPair
    fun csr(keyPair: KeyPair): PKCS10CertificationRequest
    fun certificate(request: PKCS10CertificationRequest, key: PrivateKey): Certificate
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun toCSR(encoded: ByteArray): PKCS10CertificationRequest
    fun toCertificate(encoded: ByteArray): Certificate
    fun sha256(encoded: ByteArray): ByteArray
    fun encrypt(key: PublicKey, decrypted: ByteArray): ByteArray
    fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray
    fun sign(key: PrivateKey, encoded: ByteArray): ByteArray
    fun verify(key: PublicKey, encoded: ByteArray, signature: ByteArray): Boolean
}
