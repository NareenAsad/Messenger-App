package com.example.firstmessageapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ContactsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        // Initialize Firestore and Firebase Auth
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView and Adapter
        setupRecyclerView()

        // Load Contacts from Firestore
        loadContacts()

        // Initialize and set up Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupBottomNavigation()

        // Set up Add Contact functionality
        val plusIcon: ImageView = findViewById(R.id.plus_icon)
        plusIcon.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun saveContact(contact: Contact) {
        // Saving contact to Firestore
        db.collection("users")
            .add(contact)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show()
                loadContacts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadContacts() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val contacts = result.mapNotNull { document ->
                    val userId = document.id
                    if (userId != currentUserId) {
                        val name = document.getString("name") ?: ""
                        val phoneNumber = document.getString("phoneNumber") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl")
                        Contact(userId, name, phoneNumber, profileImageUrl)
                    } else null
                }
                contactsAdapter.updateContacts(contacts)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading contacts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        val contactsRecyclerView: RecyclerView = findViewById(R.id.contactsRecyclerView)
        contactsAdapter = ContactsAdapter(mutableListOf()) { contact ->
            startChat(contact)
        }
        contactsRecyclerView.adapter = contactsAdapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun startChat(contact: Contact) {
        val intent = Intent(this, PersonalChatActivity::class.java)
        intent.putExtra("contactName", contact.name)
        intent.putExtra("contactId", contact.id)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    // Current activity, no need to restart
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, ChatsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_more -> {
                    val intent = Intent(this, MoreActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val nameInput: EditText = dialogView.findViewById(R.id.contactNameInput)
        val phoneInput: EditText = dialogView.findViewById(R.id.contactPhoneInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val name = nameInput.text.toString().trim()
                val phoneNumber = phoneInput.text.toString().trim()

                if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    val newContact = Contact("", name, phoneNumber)
                    saveContact(newContact)
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}