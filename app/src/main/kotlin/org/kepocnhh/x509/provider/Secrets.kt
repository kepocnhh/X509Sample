package org.kepocnhh.x509.provider

import java.security.KeyStore

internal interface Secrets {
    fun toKeyStore(encoded: ByteArray, password: CharArray): KeyStore
}
