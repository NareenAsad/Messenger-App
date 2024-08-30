package com.example.firstmessageapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsList: ArrayList<User>
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        contactsList = ArrayList()

        contactsAdapter = ContactsAdapter(contactsList) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userId", user.userId)
            startActivity(intent)
        }
        recyclerView.adapter = contactsAdapter

        firestore = Firebase.firestore // Initialize Firestore

        fetchUsersFromFirestore()

        // Initialize and set up Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupBottomNavigation()

        // Initialize search and add contact functionality
        setupSearchFunctionality()
        setupAddContactFunctionality()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> true
                R.id.navigation_search -> {
                    val intent = Intent(this, ChatActivity::class.java)
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

    private fun fetchUsersFromFirestore() {
        firestore.collection("users").get().addOnSuccessListener { result ->
            val newContactsList = ArrayList<User>()
            for (document in result) {
                val user = document.toObject(User::class.java)
                newContactsList.add(user)
            }
            contactsAdapter.updateContacts(newContactsList)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to load contacts: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchFunctionality() {
        val searchInput: EditText = findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterContacts(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterContacts(query: String) {
        val filteredList = contactsList.filter {
            it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query)
        }
        contactsAdapter.updateContacts(filteredList)
    }

    private fun setupAddContactFunctionality() {
        val addContactIcon: ImageView = findViewById(R.id.plus_icon)
        addContactIcon.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Contact")

        val alertDialog = dialogBuilder.show()

        val nameEditText: EditText = dialogView.findViewById(R.id.editTextName)
        val phoneEditText: EditText = dialogView.findViewById(R.id.editTextPhone)
        val saveButton: Button = dialogView.findViewById(R.id.buttonSave)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                val newUser = User(
                    userId = FirebaseAuth.getInstance().uid ?: "",
                    name = name,
                    phoneNumber = phone
                )
                saveUserToFirestore(newUser)
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.userId).set(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
        }
    }
}