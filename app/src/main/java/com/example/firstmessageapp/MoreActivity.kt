package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MoreActivity : AppCompatActivity() {

    private lateinit var settingsListView: ListView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        settingsListView = findViewById(R.id.settingsListView)
        bottomNavigation = findViewById(R.id.bottom_navigation)

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

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, settingsItems)
        settingsListView.adapter = adapter

        settingsListView.setOnItemClickListener { _, _, position, _ ->
            // Handle item click
            Toast.makeText(this, "Clicked: ${settingsItems[position]}", Toast.LENGTH_SHORT).show()
        }

        setupBottomNavigation()
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
                    val intent = Intent(this, MoreActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
