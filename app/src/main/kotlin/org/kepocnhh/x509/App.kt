package org.kepocnhh.x509

import android.app.Application
import android.content.Context
import org.kepocnhh.x509.provider.FinalAssets
import org.kepocnhh.x509.provider.FinalDirs
import org.kepocnhh.x509.provider.FinalLocals
import org.kepocnhh.x509.provider.FinalLoggers
import org.kepocnhh.x509.provider.FinalSecrets
import org.kepocnhh.x509.provider.Injection

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        _injection = Injection(
            assets = FinalAssets(context = context),
            dirs = FinalDirs(context = context),
            secrets = FinalSecrets(),
            loggers = FinalLoggers,
            locals = FinalLocals(context = context),
        )
    }

    companion object {
        private var _injection: Injection? = null
        val injection: Injection get() = checkNotNull(_injection) { "No injection!" }
    }
}
