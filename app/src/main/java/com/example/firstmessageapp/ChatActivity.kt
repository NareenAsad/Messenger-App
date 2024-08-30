package com.example.firstmessageapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var tvChatsTitle: TextView
    private lateinit var ivAddChat: ImageView
    private lateinit var ivMenu: ImageView
    private lateinit var rvStories: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var rvChatList: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var stories: MutableList<Story>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatListAdapter: ChatAdapter

    // Request code for media selection
    private val PICK_MEDIA_REQUEST_CODE = 101

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

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerViews
        setupStoriesRecyclerView()
        setupChatListRecyclerView()

        // Handle clicks
        ivAddChat.setOnClickListener {
            // Navigate to PersonalChatActivity when "Add Chat" is clicked
            val intent = Intent(this, PersonalChatActivity::class.java)
            startActivity(intent)
        }

        ivMenu.setOnClickListener {
            // Handle menu click - you can implement a popup menu or navigate to another activity
            showMenuOptions()
        }

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupStoriesRecyclerView() {
        // Initialize the mutable list of stories
        stories = mutableListOf(
            Story("Your Story", "https://example.com/your_story.jpg"),
            // Add more stories as needed
        )

        rvStories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvStories.adapter = StoriesAdapter(stories)
    }

    private fun setupChatListRecyclerView() {
        // Initialize the RecyclerView
        rvChatList.layoutManager = LinearLayoutManager(this)
        chatListAdapter = ChatAdapter(emptyList()) // Start with an empty list
        rvChatList.adapter = chatListAdapter

        // Fetch chat data from Firestore
        fetchChatsFromFirebase()
    }

    private fun fetchChatsFromFirebase() {
        firestore.collection("chats")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("ChatActivity", "Query Snapshot: ${querySnapshot.documents}")
                val chatList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Chat::class.java)
                }
                chatListAdapter.updateChats(chatList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load chats: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, ChatActivity::class.java))
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

    private fun addStory(newStory: Story) {
        // Add the new story to the list
        stories.add(newStory)

        // Notify the adapter that a new item has been added
        rvStories.adapter?.notifyItemInserted(stories.size - 1)
    }

    private fun showMenuOptions() {
        // Implement a popup menu or other menu options as needed
        Toast.makeText(this, "Menu clicked!", Toast.LENGTH_SHORT).show()
    }

    // Method to launch media picker when a story is clicked
    fun selectMediaForStory(position: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/* video/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture or Video"), PICK_MEDIA_REQUEST_CODE)
    }

    // Handle the result of the media selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedMediaUri: Uri? = data.data

            // Check if the selected media URI is not null
            if (selectedMediaUri != null) {
                val newStory = Story("New Story", selectedMediaUri.toString())
                addStory(newStory)

                // Optionally, show a message to the user
                Toast.makeText(this, "New story added: $selectedMediaUri", Toast.LENGTH_SHORT).show()
            }
        }
    }
}