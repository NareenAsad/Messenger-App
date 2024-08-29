package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChatsActivity : AppCompatActivity() {

    private lateinit var tvChatsTitle: TextView
    private lateinit var ivAddChat: ImageView
    private lateinit var ivMenu: ImageView
    private lateinit var rvStories: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var rvChatList: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize views
        tvChatsTitle = findViewById(R.id.tvChatsTitle)
        ivAddChat = findViewById(R.id.ivAddChat)
        ivMenu = findViewById(R.id.ivMenu)
        rvStories = findViewById(R.id.rvStories)
        searchBar = findViewById(R.id.search_bar)
        rvChatList = findViewById(R.id.rvChatList)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set up RecyclerViews
        setupStoriesRecyclerView()
        setupChatListRecyclerView()

        // Handle clicks
        ivAddChat.setOnClickListener {
            // Navigate to PersonalChatActivity when "Add Chat" is clicked
        }

        ivMenu.setOnClickListener {
            // Handle menu click - you can implement a popup menu or navigate to another activity
            // Example: showing a toast message
            showMenuOptions()
        }

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupStoriesRecyclerView() {
        val stories = listOf(
            Story("Your Story", "https://example.com/your_story.jpg"),
            Story("John's Story", "https://example.com/john_story.jpg"),
            Story("Jane's Story", "https://example.com/jane_story.jpg")
            // Add more stories as needed
        )

        rvStories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvStories.adapter = StoriesAdapter(stories)
    }

    private fun setupChatListRecyclerView() {
        val chats = listOf(
            Chat("John Doe", "Hey, how's it going?", "10:30 AM", 2, "https://example.com/john_profile.jpg", "uid_1"),
            Chat("Jane Smith", "Let's meet up later.", "9:45 AM", 0, "https://example.com/jane_profile.jpg", "uid_2"),
            Chat("Alice Johnson", "Did you get my email?", "8:20 AM", 5, "https://example.com/alice_profile.jpg", "uid_3")
            // Add more chats as needed
        )

        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = ChatListAdapter(chats)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.navigation_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showMenuOptions() {
        // Implement a popup menu or other menu options as needed
        // Example: Showing a simple toast message
        Toast.makeText(this, "Menu clicked!", Toast.LENGTH_SHORT).show()
    }
}
