package com.example.fingerprintactivity

import android.app.KeyguardManager
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var succeededCountTv: TextView? = null
    private var failedCountTv: TextView? = null
    private var fingerprintSharedPreferences: SharedPreferences? = null
    private var cancellationSignal: CancellationSignal? = null

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)

                val succeededCount = fingerprintSharedPreferences?.getInt("succeeded", 0)
                val editor = fingerprintSharedPreferences?.edit()
                editor?.putInt("succeeded", succeededCount!!.plus(1))
                editor?.commit()
                val succeededCountAfterEdit = fingerprintSharedPreferences?.getInt("succeeded", 0)
                succeededCountTv?.setText("Succeeded Count: " + succeededCountAfterEdit.toString())

                Toast.makeText(
                    applicationContext, "Authentication Succeeded", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)

                Toast.makeText(
                    applicationContext, "Authentication Error : $errString", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                val failedCount = fingerprintSharedPreferences?.getInt("failed", 0)
                val editor = fingerprintSharedPreferences?.edit()
                editor?.putInt("failed", failedCount!!.plus(1))
                editor?.commit()
                val failedCountAfterEdit = fingerprintSharedPreferences?.getInt("failed", 0)
                failedCountTv?.setText("Failed Count: " + failedCountAfterEdit.toString())

                Toast.makeText(
                    applicationContext, "Authentication Failed", Toast.LENGTH_SHORT
                ).show()
            }

        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        succeededCountTv = findViewById(R.id.succeededCountTv)
        failedCountTv = findViewById(R.id.failedCountTv)
        val authenticateBtn = findViewById<Button>(R.id.authenticateBtn)

        fingerprintSharedPreferences = getSharedPreferences("fingerprintPref", MODE_PRIVATE)
        val succeededCount = fingerprintSharedPreferences?.getInt("succeeded", 0)
        val failedCount = fingerprintSharedPreferences?.getInt("failed", 0)
        succeededCountTv?.setText("Succeeded Count: " + succeededCount.toString())
        failedCountTv?.setText("Failed Count: " + failedCount.toString())

        authenticateBtn.setOnClickListener {
            val isSupport = checkBiometricSupport()
            if (isSupport) {
                val biometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle("Authenticate")
                    .setSubtitle("Authenticate with fingerprint")
                    .setDescription("Put your fingerprint")
                    .setNegativeButton(
                        "Cancel",
                        mainExecutor,
                        DialogInterface.OnClickListener { dialog, which ->
                            Toast.makeText(
                                applicationContext, "Authentication Cancelled", Toast.LENGTH_SHORT
                            ).show()
                        }).build()

                biometricPrompt.authenticate(
                    getCancellationSignal(),
                    mainExecutor,
                    authenticationCallback
                )
            }

        }

    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Toast.makeText(
                applicationContext, "Authentication was Cancelled by the user", Toast.LENGTH_SHORT
            ).show()
        }
        return cancellationSignal as CancellationSignal
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) {
            Toast.makeText(
                applicationContext,
                "Fingerprint authentication has not been enabled in settings",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                applicationContext,
                "Fingerprint Authentication Permission is not enabled",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            Toast.makeText(
                applicationContext,
                "The system does not have a fingerprint feature",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

}