package com.example.firstmessageapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ContactsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        // Initialize Firestore
        db = Firebase.firestore

        // Initialize RecyclerView and Adapter
        val contactsRecyclerView: RecyclerView = findViewById(R.id.contactsRecyclerView)
        contactsAdapter = ContactsAdapter(mutableListOf()) { contact ->
            // Handle contact click
            startChat(contact)
        }
        contactsRecyclerView.adapter = contactsAdapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)

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
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val contacts = result.map { document ->
                    Contact(document.id, document.getString("name") ?: "", document.getString("phoneNumber") ?: "")
                }
                contactsAdapter.updateContacts(contacts)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading contacts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startChat(contact: Contact) {
        val intent = Intent(this, PersonalChatActivity::class.java)
        intent.putExtra("contactId", contact.id)
        intent.putExtra("contactName", contact.name)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    val intent = Intent(this, ContactsActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, ChatsActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_more -> {
                    val intent = Intent(this, MoreActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
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

data class Contact(val id: String, val name: String, val phoneNumber: String)

class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.contactName)
        val phoneTextView: TextView = view.findViewById(R.id.contactPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phoneNumber
        holder.itemView.setOnClickListener { onContactClick(contact) }
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }
}
