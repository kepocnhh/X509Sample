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
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.kepocnhh.x509.provider.Injection
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date

internal class MainActivity : ComponentActivity() {
    private class State(
        val key: PrivateKey,
        val csr: PKCS10CertificationRequest,
        val crt: Certificate,
    )

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
        state: State,
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
                value = injection.secrets.sha256(state.key.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            view.text(
                title = "csr:",
                value = injection.secrets.sha256(state.csr.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            check(state.crt is X509Certificate)
            check(state.crt.notBefore.before(Date()))
            check(state.crt.notAfter.after(Date()))
            view.text(
                title = "certificate:",
                value = injection.secrets.sha256(state.crt.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            view.text(
                title = "not before:",
                value = state.crt.notBefore.toString(),
            )
            view.text(
                title = "not after:",
                value = state.crt.notAfter.toString(),
            )
            view.text(
                title = "public key:",
                value = injection.secrets.sha256(state.crt.publicKey.encoded).hex(),
                typeface = Typeface.MONOSPACE,
            )
            //
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
            val signature = injection.secrets.sign(key = state.key, encoded = encoded)
            view.text(
                title = "signature:",
                value = injection.secrets.sha256(signature).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val encrypted = injection.secrets.encrypt(key = state.crt.publicKey, decrypted = encoded)
            view.text(
                title = "encrypted:",
                value = injection.secrets.sha256(encrypted).hex(),
                typeface = Typeface.MONOSPACE,
            )
            val decrypted = injection.secrets.decrypt(key = state.key, encrypted = encrypted)
            view.text(
                title = "decrypted:",
                value = injection.secrets.sha256(decrypted).hex(),
                typeface = Typeface.MONOSPACE,
            )
            check(injection.secrets.verify(key = state.crt.publicKey, encoded = decrypted, signature = signature))
            view.text(
                title = "decoded:",
                value = String(decrypted),
            )
            Button(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "clear"
                it.setOnClickListener { _ ->
                    injection.dirs.files.resolve("key.der").delete()
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
            check(crt is X509Certificate)
            check(crt.notBefore.before(Date()))
            check(crt.notAfter.after(Date()))
            check(injection.secrets.verify(key = crt.publicKey, encoded = crt.encoded, signature = crt.signature))
            view.text(
                title = "not before:",
                value = crt.notBefore.toString(),
            )
            view.text(
                title = "not after:",
                value = crt.notAfter.toString(),
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
            Button(context).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                it.text = "generate"
                it.setOnClickListener { _ ->
                    val keyPair = injection.secrets.newKeyPair()
                    injection.dirs.files.resolve("key.der").also { file ->
                        file.writeBytes(keyPair.private.encoded)
                    }
                    val csr = injection.secrets.csr(keyPair = keyPair)
                    injection.dirs.files.resolve("csr.der").also { file ->
                        file.writeBytes(csr.encoded)
                    }
                    val crt = injection.secrets.certificate(request = csr, key = keyPair.private)
                    injection.dirs.files.resolve("crt.der").also { file ->
                        file.writeBytes(crt.encoded)
                    }
                    onKeys(
                        context = context,
                        injection = injection,
                        root = root,
                        state = State(
                            key = keyPair.private,
                            csr = csr,
                            crt = crt,
                        ),
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
            State(
                key = injection.dirs.files.resolve("key.der").let {
                    injection.secrets.toPrivateKey(it.readBytes())
                },
                csr = injection.dirs.files.resolve("csr.der").let {
                    injection.secrets.toCSR(it.readBytes())
                },
                crt = injection.dirs.files.resolve("crt.der").let {
                    injection.secrets.toCertificate(it.readBytes())
                },
            )
        }.fold(
            onSuccess = { state ->
                onKeys(
                    context = context,
                    injection = injection,
                    root = root,
                    state = state,
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
