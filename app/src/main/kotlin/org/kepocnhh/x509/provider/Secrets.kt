package org.kepocnhh.x509.provider

import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate

internal interface Secrets {
    fun toKeyStore(encoded: ByteArray, password: CharArray): KeyStore
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun sha256(encoded: ByteArray): ByteArray
    fun setCertificate(alias: String, crt: Certificate)
    fun getCertificate(alias: String): Certificate?
    fun deleteEntry(alias: String)
    fun encrypt(crt: Certificate, decrypted: ByteArray): ByteArray
    fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray
}
