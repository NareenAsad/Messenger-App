package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MoreActivity : AppCompatActivity() {

    private lateinit var settingsListView: ListView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var phoneTextView: TextView

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        settingsListView = findViewById(R.id.settingsListView)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Fetch and display user info
        fetchUserData()

        val settingsItems = listOf(
            "Account",
            "Chats",
            "Appearance",
            "Notification",
            "Privacy",
            "Data Usage",
            "Help",
            "Invite Your Friends"
        )

        val icons = listOf(
            R.drawable.outline_person,
            R.drawable.round_chat_bubble_outline_24,
            R.drawable.wb_sunny,
            R.drawable.ic_notifications,
            R.drawable.ic_privacy,
            R.drawable.ic_data_usage,
            R.drawable.ic_help,
            R.drawable.ic_invite
        )

        val adapter = SettingsAdapter(this, settingsItems, icons)
        settingsListView.adapter = adapter

        settingsListView.setOnItemClickListener { _, _, position, _ ->
            // Handle item click
            Toast.makeText(this, "Clicked: ${settingsItems[position]}", Toast.LENGTH_SHORT).show()
        }

        setupBottomNavigation()
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                nameTextView.text = name ?: "Set your name"
                phoneTextView.text = phone ?: "Set your phone number"

                if (profileImageUrl != null) {
                    Glide.with(this@MoreActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.profile)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MoreActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    // Navigate to ContactsActivity
                    val intent = Intent(this, ContactsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    // Navigate to ChatActivity
                    val intent = Intent(this, ChatsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_more -> {
                    // Navigate to MoreActivity
                    true
                }
                else -> false
            }
        }
    }

    class SettingsAdapter(context: Context, private val items: List<String>, private val icons: List<Int>) :
        ArrayAdapter<String>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_setting, parent, false)
            val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
            val titleTextView: TextView = view.findViewById(R.id.titleTextView)

            titleTextView.text = items[position]
            iconImageView.setImageResource(icons[position])

            return view
        }
    }
}