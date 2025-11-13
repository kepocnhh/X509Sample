package org.kepocnhh.x509

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.kepocnhh.x509.provider.Injection
import java.security.PrivateKey
import java.security.PublicKey
import javax.security.cert.Certificate

internal class MainActivity : ComponentActivity() {
    private fun onKeys(
        context: Context,
        injection: Injection,
        root: FrameLayout,
        key: PrivateKey,
        pub: PublicKey,
    ) {
        LinearLayout(context).also { view ->
            view.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL,
            )
            view.orientation = LinearLayout.VERTICAL
            injection.secrets.sha256(key.encoded).also { hash ->
                TextView(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    it.text = "private key:"
                    view.addView(it)
                }
                TextView(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    it.text = hash.joinToString(separator = "") { byte ->
                        String.format("%02x", byte.toInt().and(0xff))
                    }
                    it.typeface = Typeface.MONOSPACE
                    view.addView(it)
                }
            }
            injection.secrets.sha256(pub.encoded).also { hash ->
                TextView(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    it.text = "public key:"
                    view.addView(it)
                }
                TextView(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    it.text = hash.joinToString(separator = "") { byte ->
                        String.format("%02x", byte.toInt().and(0xff))
                    }
                    it.typeface = Typeface.MONOSPACE
                    view.addView(it)
                }
            }
            root.addView(view)
        }
    }

    private fun noKeys(
        context: Context,
        injection: Injection,
        root: FrameLayout,
    ) {
        LinearLayout(context).also { view ->
            view.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL,
            )
            view.orientation = LinearLayout.VERTICAL
            TextView(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "name"
                view.addView(it)
            }
            val names = EditText(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.setText("foo.p12")
                view.addView(it)
            }
            TextView(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "alias"
                view.addView(it)
            }
            val aliases = EditText(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.setText("foo")
                view.addView(it)
            }
            TextView(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "password"
                view.addView(it)
            }
            val passwords = EditText(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.setText("qwe123")
                view.addView(it)
            }
            Button(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "open"
                it.setOnClickListener { _ ->
                    runCatching {
                        val name = names.text?.toString() ?: error("No name!")
                        val alias = aliases.text?.toString() ?: error("No alias!")
                        val encoded = injection.assets.getAsset(name = name).use { stream ->
                            stream.readBytes()
                        }
                        val password = passwords.text?.toString().orEmpty().toCharArray()
                        val keyStore = injection.secrets.toKeyStore(
                            encoded = encoded,
                            password = password,
                        )
                        val key = keyStore.getKey(alias, password) ?: error("No key!")
                        check(key is PrivateKey)
                        val crt = keyStore.getCertificate(alias) ?: error("No crt!")
                        val pub = crt.publicKey
                        injection.dirs.cache.resolve("rsa.key").also { file ->
                            file.writeBytes(key.encoded)
                        }
                        injection.dirs.cache.resolve("rsa.crt").also { file ->
                            file.writeBytes(crt.encoded)
                        }
                        injection.dirs.cache.resolve("rsa.pub").also { file ->
                            file.writeBytes(pub.encoded)
                        }
                    }.fold(
                        onSuccess = {
                            // todo
                        },
                        onFailure = { error ->
                            logger.warning("Read keystore error: $error")
                        },
                    )
                }
                view.addView(it)
            }
            root.addView(view)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val injection = App.injection
        val root = FrameLayout(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        runCatching {
            val key = injection.dirs.cache.resolve("rsa.key").let {
                injection.secrets.toPrivateKey(it.readBytes())
            }
            val pub = injection.dirs.cache.resolve("rsa.pub").let {
                injection.secrets.toPublicKey(it.readBytes())
            }
            key to pub
        }.fold(
            onSuccess = { (key, pub) ->
                onKeys(
                    context = context,
                    injection = injection,
                    root = root,
                    key = key,
                    pub = pub,
                )
            },
            onFailure = {
                noKeys(
                    context = context,
                    injection = injection,
                    root = root,
                )
            },
        )
        setContentView(root)
    }

    companion object {
        val logger = App.injection.loggers.create("[Main]")
    }
}
