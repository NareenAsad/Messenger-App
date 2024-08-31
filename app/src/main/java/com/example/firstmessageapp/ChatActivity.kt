package com.example.firstmessageapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ChatActivity : AppCompatActivity() {

    private lateinit var tvChatsTitle: TextView
    private lateinit var ivAddChat: ImageView
    private lateinit var ivMenu: ImageView
    private lateinit var searchBar: EditText
    private lateinit var rvChatList: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var storageReference: StorageReference
    private lateinit var chatListAdapter: ChatAdapter

    // Firebase Database reference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    // List to store all chats fetched from Firebase
    private val allChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        tvChatsTitle = findViewById(R.id.tvChatsTitle)
        ivAddChat = findViewById(R.id.ivAddChat)
        ivMenu = findViewById(R.id.ivMenu)
        searchBar = findViewById(R.id.search_bar)
        rvChatList = findViewById(R.id.rvChatList)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().reference

        setupChatListRecyclerView()

        // Handle clicks
        ivAddChat.setOnClickListener {
            val intent = Intent(this, PersonalChatActivity::class.java)
            startActivity(intent)
        }

        ivMenu.setOnClickListener {
            showMenuOptions()
        }

        setupBottomNavigation()
        setupSearchFunctionality()
    }

    private fun setupChatListRecyclerView() {
        chatListAdapter = ChatAdapter(mutableListOf())
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = chatListAdapter

        fetchChatsFromFirebase()
    }

    private fun fetchChatsFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val chatsRef = database.getReference("Chats")

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allChats.clear()
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    chat?.let {
                        if (it.user1Name == userId || it.user2Name == userId) {
                            allChats.add(it)
                        }
                    }
                }
                // Initially show all chats
                chatListAdapter.updateChats(allChats)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Failed to load chats", error.toException())
                Toast.makeText(this@ChatActivity, "Failed to load chats", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchFunctionality() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChats(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterChats(query: String) {
        val filteredChats = if (query.isEmpty()) {
            allChats
        } else {
            allChats.filter {
                it.user1Name.contains(query, true) || it.user2Name.contains(query, true)
            }
        }
        chatListAdapter.updateChats(filteredChats)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
                R.id.navigation_search -> true
                R.id.navigation_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showMenuOptions() {
        Toast.makeText(this, "Menu clicked!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up Firebase listeners if needed
        // For example: databaseRef.removeEventListener(yourListener)
    }
}