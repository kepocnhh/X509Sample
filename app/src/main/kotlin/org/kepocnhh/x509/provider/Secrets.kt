package org.kepocnhh.x509.provider

import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

internal interface Secrets {
    fun toKeyStore(encoded: ByteArray, password: CharArray): KeyStore
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun sha256(encoded: ByteArray): ByteArray
}
