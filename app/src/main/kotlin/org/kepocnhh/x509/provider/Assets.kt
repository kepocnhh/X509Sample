package org.kepocnhh.x509.provider

import java.io.InputStream

internal interface Assets {
    fun getAsset(name: String): InputStream
}
