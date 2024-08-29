package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.firstmessageapp.databinding.VerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerificationCodeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var binding: VerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.verification)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId") ?: ""

        setupUI()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.resendCodeButton.setOnClickListener {
            // Implement resend code logic here
            Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
            // Call a method to trigger the resend (not provided in your original code)
        }

        val codeDigits = listOf(
            binding.codeDigit1,
            binding.codeDigit2,
            binding.codeDigit3,
            binding.codeDigit4,
            binding.codeDigit5,
            binding.codeDigit6
        )

        binding.verifyButton.setOnClickListener {
            val code = codeDigits.joinToString("") { it.text.toString() }
            if (code.length == 6) {
                verifyPhoneNumberWithCode(verificationId, code)
            } else {
                Toast.makeText(this, "Please enter a valid code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}