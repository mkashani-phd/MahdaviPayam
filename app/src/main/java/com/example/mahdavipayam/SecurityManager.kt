package com.example.mahdavipayam

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurityManager {
    private const val PREFS_NAME = "secure_root_storage"
    private const val ROOT_KEY_ALIAS = "root_public_key"

    private fun getEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(true) 
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeRootPublicKey(context: Context, publicKey: String): Boolean {
        val prefs = getEncryptedPrefs(context)
        if (prefs.contains(ROOT_KEY_ALIAS)) return false 
        prefs.edit().putString(ROOT_KEY_ALIAS, publicKey).apply()
        return true
    }

    fun getRootPublicKey(context: Context): String? {
        return getEncryptedPrefs(context).getString(ROOT_KEY_ALIAS, null)
    }

    fun getPublicKey(context: Context, keyId: Any): ByteArray? {
        // We currently support "andriod" or 0 as root key
        if (keyId == 0 || keyId == "andriod") {
            val keyStr = getRootPublicKey(context) ?: return null
            return try {
                val parts = keyStr.split(" ")
                val base64Key = if (parts.size >= 2) parts[1] else parts[0]
                val fullKeyBytes = Base64.decode(base64Key, Base64.DEFAULT)
                
                if (fullKeyBytes.size == 32) {
                    fullKeyBytes
                } else if (fullKeyBytes.size > 32) {
                    // Extract the last 32 bytes from ssh-ed25519 format
                    fullKeyBytes.takeLast(32).toByteArray()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun isKeyProvisioned(context: Context): Boolean {
        return getEncryptedPrefs(context).contains(ROOT_KEY_ALIAS)
    }
}
