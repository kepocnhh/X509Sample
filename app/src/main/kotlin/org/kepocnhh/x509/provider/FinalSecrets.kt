package org.kepocnhh.x509.provider

import java.security.KeyStore

internal class FinalSecrets : Secrets {
    override fun toKeyStore(
        encoded: ByteArray,
        password: CharArray,
    ): KeyStore {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(encoded.inputStream(), password)
        return keyStore
    }
}
