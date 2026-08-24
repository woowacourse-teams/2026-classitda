package com.classitda.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.classitda.domain.model.auth.signup.LoginTokens
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreAuthTokenStorage(
    context: Context,
) : AuthTokenStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): LoginTokens? =
        runCatching {
            val encoded = preferences.getString(TOKEN_KEY, null) ?: return null
            val decoded = decrypt(encoded).split(DELIMITER)
            LoginTokens(
                accessToken = decoded[0],
                accessTokenExpiresInSeconds = decoded[1].toLong(),
                refreshToken = decoded[2],
                refreshTokenExpiresInSeconds = decoded[3].toLong(),
            )
        }.getOrNull()

    override fun write(tokens: LoginTokens) {
        val payload =
            listOf(
                tokens.accessToken,
                tokens.accessTokenExpiresInSeconds,
                tokens.refreshToken,
                tokens.refreshTokenExpiresInSeconds,
            ).joinToString(DELIMITER)
        preferences.edit().putString(TOKEN_KEY, encrypt(payload)).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val encrypted = Base64.decode(value, Base64.NO_WRAP)
        val iv = encrypted.copyOfRange(0, IV_LENGTH)
        val payload = encrypted.copyOfRange(IV_LENGTH, encrypted.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(payload).toString(Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER,
            )
        val keySpecBuilder =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
        keySpecBuilder.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        keySpecBuilder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        keyGenerator.init(keySpecBuilder.build())
        return keyGenerator.generateKey()
    }

    private companion object {
        const val PREFERENCES_NAME = "auth_tokens"
        const val TOKEN_KEY = "encrypted_tokens"
        const val KEY_ALIAS = "classitda_auth_tokens"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
        const val DELIMITER = "\u001F"
    }
}
