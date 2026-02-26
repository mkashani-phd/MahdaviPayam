package com.example.mahdavipayam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mahdavipayam.ui.theme.MahdaviPayamTheme
import java.security.MessageDigest
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private val EXPECTED_SIGNATURE_HASH = "TF3WMvBqBKiKZf0UBrp9XL01OoXoveQOlOVnehSgXmc="

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val raw = result.data?.getStringExtra("QR_RAW")
        if (raw != null) {
            verifyAndDisplay(raw)
        } else {
            qrResult.value = "داده‌ای از QR دریافت نشد."
        }
    }

    private fun verifyAndDisplay(rawJson: String) {
        val result = QRVerifier.verify(this, rawJson)
        when (result) {
            is QRVerifier.VerificationResult.Success -> {
                qrResult.value = result.message
                verifiedKeyAlias.value = result.keyAlias
                isVerified.value = true
                showResultDialog.value = true
            }
            is QRVerifier.VerificationResult.Failure -> {
                qrResult.value = "خطا: ${result.error}"
                verifiedKeyAlias.value = ""
                isVerified.value = false
                showResultDialog.value = true
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startScanner()
        } else {
            Toast.makeText(this, "دسترسی به دوربین برای اسکن لازم است", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        isAuthentic.value = isAppAuthentic()

        if (isAuthentic.value) {
            setupRootKey()
            // Automatically start scanner on launch
            checkPermissionAndStartScanner()
        }

        setContent {
            MahdaviPayamTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isAuthentic.value) {
                        val storedKey = SecurityManager.getRootPublicKey(this) ?: "تنظیم نشده"
                        MainScreen(
                            rootKey = storedKey,
                            onScanClick = {
                                checkPermissionAndStartScanner()
                            }
                        )
                    } else {
                        ErrorScreen()
                    }
                }
            }
        }
    }

    private fun isAppAuthentic(): Boolean {
        try {
            val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.signingCertificateHistory
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            }

            if (signatures == null) return false

            for (sig in signatures) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(sig.toByteArray())
                val currentSignatureHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                
                Log.d("AppAuthenticity", "Current Signature Hash: $currentSignatureHash")
                
                if (currentSignatureHash == EXPECTED_SIGNATURE_HASH) return true
            }
        } catch (e: Exception) { 
            Log.e("AppAuthenticity", "Error checking signature", e)
        }
        return false
    }

    private fun setupRootKey() {
        val currentKey = SecurityManager.getRootPublicKey(this)
        if (currentKey == null) {
            val rootKey = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIF7VF4yC9QKvYKjexcduhbH0R/ADxkZArVNzfSqiUcM/ andriod app test"
            SecurityManager.storeRootPublicKey(this, rootKey)
        }
    }

    private fun checkPermissionAndStartScanner() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startScanner()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startScanner() {
        scanLauncher.launch(Intent(this, ScannerActivity::class.java))
    }

    companion object {
        val qrResult = mutableStateOf("")
        val verifiedKeyAlias = mutableStateOf("")
        val isVerified = mutableStateOf(false)
        val isAuthentic = mutableStateOf(true)
        val showResultDialog = mutableStateOf(false)
    }
}

@Composable
fun ErrorScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️ خطا در امنیت برنامه",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "این نسخه از اپلیکیشن معتبر نیست و ممکن است دستکاری شده باشد.\n\n" +
                   "برای حفظ امنیت و اطمینان از صحت پیام‌ها، لطفا فقط از نسخه رسمی استفاده کنید.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { exitProcess(0) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("بستن برنامه", color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
fun MainScreen(rootKey: String, onScanClick: () -> Unit) {
    if (MainActivity.showResultDialog.value) {
        AlertDialog(
            onDismissRequest = { MainActivity.showResultDialog.value = false },
            title = {
                Text(
                    text = if (MainActivity.isVerified.value) "✅ پیام تایید شد" else "❌ عدم تایید",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = MainActivity.qrResult.value,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (MainActivity.isVerified.value && MainActivity.verifiedKeyAlias.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "تایید شده توسط: ${MainActivity.verifiedKeyAlias.value}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    MainActivity.showResultDialog.value = false
                    onScanClick() 
                }) {
                    Text("اسکن مجدد")
                }
            },
            dismissButton = {
                TextButton(onClick = { MainActivity.showResultDialog.value = false }) {
                    Text("بستن")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "کلید عمومی ریشه (امن شده):",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = rootKey,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("شروع اسکن")
            }
        }

        Text(
            text = "powered by freeiranprotests.com",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
