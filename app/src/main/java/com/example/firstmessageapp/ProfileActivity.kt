package com.example.firstmessageapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImage: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val firstNameInput: EditText = findViewById(R.id.first_name_input)
        val lastNameInput: EditText = findViewById(R.id.last_name_input)
        val saveButton: Button = findViewById(R.id.continueButton)
        val uploadButton: Button = findViewById(R.id.upload_button)
        val backButton: ImageView = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profile_image)

        backButton.setOnClickListener {
            onBackPressed()
        }

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1000)
        }

        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && imageUri != null) {
                uploadImageAndSaveProfile(firstName, lastName)
            } else {
                Toast.makeText(this, "Please enter a name and select an image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            if (imageUri != null) {
                profileImage.setImageURI(imageUri)
                Log.d("ProfileActivity", "Image selected: $imageUri")
            } else {
                Log.e("ProfileActivity", "Failed to select image")
                Toast.makeText(this, "Failed to select image. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageAndSaveProfile(firstName: String, lastName: String) {
        val user = auth.currentUser
        if (user != null && imageUri != null) {
            val ref = storage.reference.child("profileImages/${user.uid}.jpg")
            Log.d("ProfileActivity", "Uploading image to: ${ref.path}")
            val uploadTask = ref.putFile(imageUri!!)

            uploadTask.addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val userProfile = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "profileImageUrl" to uri.toString()
                    )

                    // Save the data in Firebase Realtime Database
                    database.getReference("users").child(user.uid)
                        .setValue(userProfile)
                        .addOnSuccessListener {
                            Log.d("ProfileActivity", "Profile successfully saved.")
                            startActivity(Intent(this, ContactsActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileActivity", "Error saving profile", e)
                            Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Error getting download URL", e)
                    Toast.makeText(this, "Error getting download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error uploading image", e)
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated or imageUri is null", Toast.LENGTH_SHORT).show()
            Log.e("ProfileActivity", "User not authenticated or imageUri is null. User: $user, imageUri: $imageUri")
        }
    }
}
