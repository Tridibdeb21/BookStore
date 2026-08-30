package com.example.bookstore.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

object CryptoUtils {

    private fun generateKey(passphrase: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        var key = passphrase.toByteArray(Charsets.UTF_8)
        key = sha.digest(key)
        return SecretKeySpec(key, "AES")
    }

    fun encrypt(plainText: String, secretKey: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val keySpec = generateKey(secretKey)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }

    fun decrypt(cipherText: String, secretKey: String): String {
        if (cipherText.isBlank()) return ""
        return try {
            val keySpec = generateKey(secretKey)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "[Unable to decrypt note]"
        }
    }
}
