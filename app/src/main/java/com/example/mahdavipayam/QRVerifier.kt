package com.example.mahdavipayam

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.subtle.Ed25519Verify
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object QRVerifier {

    data class QRData(
        @SerializedName("k") val keyAlias: String,
        @SerializedName("m") val message: String,
        @SerializedName("s") val signature: String
    )

    sealed class VerificationResult {
        data class Success(val message: String, val keyAlias: String) : VerificationResult()
        data class Failure(val error: String) : VerificationResult()
    }

    /**
     * Verifies the QR code data.
     * Expects JSON format: {"k": "keyAlias", "m": "message", "s": "signature"}
     */
    fun verify(context: Context, jsonString: String): VerificationResult {
        return try {
            val data = Gson().fromJson(jsonString, QRData::class.java)
            val messageBytes = data.message.toByteArray()
            
            // Signature in example uses URL-safe Base64 (contains '-')
            val signatureBytes = Base64.decode(data.signature, Base64.URL_SAFE)
            
            val publicKey = SecurityManager.getPublicKey(context, data.keyAlias) 
                ?: return VerificationResult.Failure("Unknown Key: ${data.keyAlias}")
            
            val verifier = Ed25519Verify(publicKey)
            try {
                verifier.verify(signatureBytes, messageBytes)
                VerificationResult.Success(data.message, data.keyAlias)
            } catch (e: Exception) {
                VerificationResult.Failure("Signature Verification Failed")
            }
        } catch (e: Exception) {
            VerificationResult.Failure("Invalid QR Format: ${e.message}")
        }
    }
}
