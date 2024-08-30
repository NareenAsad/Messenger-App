package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.example.firstmessageapp.databinding.ActivityVerifyCodeBinding

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var binding: ActivityVerifyCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId")!!

        setupUI()
    }

    private fun setupUI() {
        // Handle back button click
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Handle verify button click
        binding.buttonVerifyCode.setOnClickListener {
            val code = listOf(
                binding.codeDigit1,
                binding.codeDigit2,
                binding.codeDigit3,
                binding.codeDigit4,
                binding.codeDigit5,
                binding.codeDigit6
            ).joinToString("") {
                it.text.toString()
            }

            if (code.length == 6) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        // Navigate to the Contacts screen
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Verification failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}