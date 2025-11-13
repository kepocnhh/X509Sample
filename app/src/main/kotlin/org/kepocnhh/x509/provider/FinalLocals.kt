package org.kepocnhh.x509.provider

import android.content.Context
import org.kepocnhh.x509.BuildConfig

internal class FinalLocals(context: Context) : Locals {
    private val prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    override var alias: String?
        get() = prefs.getString("alias", null)
        set(value) {
            prefs.edit().putString("alias", value).commit()
        }
}