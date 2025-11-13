package org.kepocnhh.x509.provider

import android.content.Context

internal class FinalDirs(context: Context) : Dirs {
    override val files = context.filesDir!!
}
