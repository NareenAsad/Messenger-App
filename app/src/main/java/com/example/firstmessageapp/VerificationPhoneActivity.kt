package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class VerificationPhoneActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_verification)

        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()

        val continueButton: Button = findViewById(R.id.continueButton)
        val phoneNumberInput: EditText = findViewById(R.id.phoneNumberInput)

        continueButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                Log.d("phoneno", phoneNumber)
                startPhoneNumberVerification(phoneNumber)
            } else {
                phoneNumberInput.error = "Please enter a valid phone number"
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.d("verificationcode", e.message.toString())
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    val intent = Intent(this@VerificationPhoneActivity, VerificationCodeActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    Log.d("verificationcode", verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    val user = auth.currentUser
                    if (user != null) {
                        // Store user information in Firestore
                        storeUserInformation(user.phoneNumber)
                    }
                } else {
                    // Sign-in failed, display a message to the user
                }
            }
    }

    private fun storeUserInformation(phoneNumber: String?) {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        val userMap = mapOf(
            "phoneNumber" to phoneNumber
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                // Successfully stored user information
            }
            .addOnFailureListener { e ->
                Log.d("storeUserInfo", "Error storing user information: ${e.message}")
            }
    }
}
