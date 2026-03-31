package com.example.bluepay.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.Base64

class SecurityManager {
    private val keyAlias = "BluePayPaymentKey"
    private val provider = "AndroidKeyStore"

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(provider).apply { load(null) }
        if (!keyStore.containsAlias(keyAlias)) {
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, provider)
            val parameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256)
                setUserAuthenticationRequired(true) // Requires Biometric for every use
                setInvalidatedByBiometricEnrollment(true)
                build()
            }
            kpg.initialize(parameterSpec)
            kpg.generateKeyPair()
        }
    }

    fun getSignatureInstance(): Signature {
        val keyStore = KeyStore.getInstance(provider).apply { load(null) }
        val privateKey = keyStore.getKey(keyAlias, null) as java.security.PrivateKey
        return Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
        }
    }

    fun signData(data: String, signature: Signature): String {
        signature.update(data.toByteArray())
        return Base64.getEncoder().encodeToString(signature.sign())
    }
}
