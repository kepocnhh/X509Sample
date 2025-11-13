package org.kepocnhh.x509.provider

import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate

internal interface Secrets {
    fun toKeyStore(encoded: ByteArray, password: CharArray): KeyStore
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun toCertificate(encoded: ByteArray): Certificate
    fun sha256(encoded: ByteArray): ByteArray
}
