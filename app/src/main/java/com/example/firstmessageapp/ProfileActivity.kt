package com.example.firstmessageapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstmessageapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var selectedPhotoUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val PICK_IMAGE_REQUEST = 1000
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.continueButton.setOnClickListener {
            performProfileUpdate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Log.d(TAG, "Photo was selected")

            // Load and display the selected image
            selectedPhotoUri?.let { uri ->
                Picasso.get().load(uri).into(binding.profileImage)
            }
        }
    }

    private fun performProfileUpdate() {
        val firstName = binding.firstNameInput.text.toString().trim()
        val lastName = binding.lastNameInput.text.toString().trim()

        if (firstName.isEmpty()) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator (you might want to add a ProgressBar in your layout)
        // binding.loadingView.visibility = View.VISIBLE

        uploadImageToFirebaseStorage(firstName, lastName)
    }

    private fun uploadImageToFirebaseStorage(firstName: String, lastName: String) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference("/profile_images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Successfully uploaded image: ${taskSnapshot.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "File Location: $uri")
                    saveUserToFirebaseDatabase(firstName, lastName, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to upload image to storage: ${e.message}")
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                // binding.loadingView.visibility = View.GONE
            }
    }

    private fun saveUserToFirebaseDatabase(firstName: String, lastName: String, profileImageUrl: String) {
        val uid = auth.uid ?: return
        val ref = database.getReference("/users/$uid")

        val fullName = "$firstName $lastName"
        val user = User(uid, fullName, profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User profile saved to Firebase Database")

                // Save profile info to the Users node
                saveProfileInfo(fullName, profileImageUrl)

                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Navigate to the next activity (e.g., ContactsActivity)
                val intent = Intent(this, ContactsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to set value to database: ${e.message}")
                Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                // binding.loadingView.visibility = View.GONE
            }
    }

    private fun saveProfileInfo(name: String, profileImageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            userRef.child("name").setValue(name)
            userRef.child("profileImageUrl").setValue(profileImageUrl)
                .addOnSuccessListener {
                    Log.d(TAG, "Profile info saved to Users node")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save profile info to Users node: ${e.message}")
                }
        }
    }
}