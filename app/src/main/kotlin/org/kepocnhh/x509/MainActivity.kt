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
import java.security.cert.Certificate

internal class MainActivity : ComponentActivity() {
    private fun ByteArray.hex(): String {
        return joinToString(separator = "") { byte ->
            String.format("%02x", byte.toInt().and(0xff))
        }
    }

    private fun LinearLayout.text(title: String, value: String, typeface: Typeface = Typeface.DEFAULT) {
        TextView(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            it.text = title
            addView(it)
        }
        TextView(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            it.text = value
            it.typeface = typeface
            addView(it)
        }
    }

    private fun onKeys(
        context: Context,
        injection: Injection,
        root: FrameLayout,
        key: PrivateKey,
        crt: Certificate,
    ) {
        root.removeAllViews()
        LinearLayout(context).also { view ->
            view.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL,
            )
            view.orientation = LinearLayout.VERTICAL
            view.text(
                title = "private key:",
                value = injection.secrets.sha256(key.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            view.text(
                title = "public key:",
                value = injection.secrets.sha256(crt.publicKey.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val payload = System.currentTimeMillis().toString()
            view.text(
                title = "payload:",
                value = payload,
            )
            val encoded = payload.toByteArray()
            view.text(
                title = "encoded:",
                value = injection.secrets.sha256(encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val signature = injection.secrets.sign(key = key, encoded = encoded)
            view.text(
                title = "signature:",
                value = injection.secrets.sha256(signature).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val encrypted = injection.secrets.encrypt(key = crt.publicKey, decrypted = encoded)
            view.text(
                title = "encrypted:",
                value = injection.secrets.sha256(encrypted).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val decrypted = injection.secrets.decrypt(key = key, encrypted = encrypted)
            view.text(
                title = "decrypted:",
                value = injection.secrets.sha256(decrypted).hex(),
                typeface = Typeface.MONOSPACE,
            )
            check(injection.secrets.verify(key = crt.publicKey, encoded = decrypted, signature = signature))
            view.text(
                title = "decoded:",
                value = String(decrypted),
            )
            //
            Button(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "clear"
                it.setOnClickListener { _ ->
                    injection.dirs.files.resolve("rsa.key").delete()
                    val alias = injection.locals.alias ?: error("No alias!")
                    injection.secrets.deleteEntry(alias = alias)
                    injection.locals.alias = null
                    noKeys(
                        context = context,
                        injection = injection,
                        root = root,
                    )
                }
                view.addView(it)
            }
            root.addView(view)
        }
    }

    private fun noKeys(
        context: Context,
        injection: Injection,
        root: FrameLayout,
    ) {
        root.removeAllViews()
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
                        injection.dirs.files.resolve("rsa.key").also { file ->
                            file.writeBytes(key.encoded)
                        }
                        injection.secrets.setCertificate(alias = alias, crt = crt)
                        injection.locals.alias = alias
                        key to crt
                    }.fold(
                        onSuccess = { (key, crt) ->
                            onKeys(
                                context = context,
                                injection = injection,
                                root = root,
                                key = key,
                                crt = crt,
                            )
                        },
                        onFailure = { error ->
                            logger.warning("keystore error: $error")
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
            val alias = injection.locals.alias ?: error("No alias!")
            val key = injection.dirs.files.resolve("rsa.key").let {
                injection.secrets.toPrivateKey(it.readBytes())
            }
            val crt = injection.secrets.getCertificate(alias = alias) ?: error("No certificate!")
            key to crt
        }.fold(
            onSuccess = { (key, crt) ->
                onKeys(
                    context = context,
                    injection = injection,
                    root = root,
                    key = key,
                    crt = crt,
                )
            },
            onFailure = { error ->
                logger.warning("read error: $error")
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
