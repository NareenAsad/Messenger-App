package com.example.firstmessageapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsList: ArrayList<User>
    private lateinit var bottomNavigation: BottomNavigationView
    private val currentUserId: String by lazy { FirebaseAuth.getInstance().uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        contactsList = ArrayList()

        contactsAdapter = ContactsAdapter(contactsList) { user ->
            val intent = Intent(this, PersonalChatActivity::class.java)
            intent.putExtra("userId", user.userId)
            startActivity(intent)
        }
        recyclerView.adapter = contactsAdapter

        // Fetch users from Realtime Database
        fetchUsersFromDatabase()

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

    private fun fetchUsersFromDatabase() {
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newContactsList = ArrayList<User>()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null && user.userId != currentUserId) {
                        newContactsList.add(user)
                    }
                }
                contactsAdapter.updateContacts(newContactsList)
                Log.d("ContactsActivity", "Users loaded: ${newContactsList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactsActivity", "Failed to load contacts", error.toException())
                Toast.makeText(this@ContactsActivity, "Failed to load contacts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
            it.name.contains(query, ignoreCase = true)
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
        val saveButton: Button = dialogView.findViewById(R.id.buttonSave)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()

            if (name.isNotEmpty()) {
                val newUser = User(
                    userId = FirebaseAuth.getInstance().uid ?: "",
                    name = name,
                    profileImageUrl = "", // Provide default or empty URL if not available
                    isOnline = false, // Default value, update as needed
                    lastSeen = null  // Default value, update as needed
                )
                saveUserToDatabase(newUser)
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToDatabase(user: User) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(user.userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
        }
    }
}