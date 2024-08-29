package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Call the function to navigate after a delay
        navigateToVerificationAfterDelay()
    }

    private fun navigateToVerificationAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToVerificationActivity()
        }, 3000) // 3000 milliseconds = 3 seconds
    }

    private fun navigateToVerificationActivity() {
        val intent = Intent(this, VerificationPhoneActivity::class.java)
        startActivity(intent)
        finish() // This will close the MainActivity so the user can't go back to it
    }
}