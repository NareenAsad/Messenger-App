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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

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
    private lateinit var storageReference: StorageReference
    private lateinit var chatListAdapter: ChatAdapter
    private lateinit var storiesAdapter: StoriesAdapter

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

        // Initialize Firestore and Firebase Storage
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        // Set up RecyclerViews
        setupStoriesRecyclerView()
        setupChatListRecyclerView()

        // Load stories from Firebase
        loadStories()

        // Handle clicks
        ivAddChat.setOnClickListener {
            val intent = Intent(this, PersonalChatActivity::class.java)
            startActivity(intent)
        }

        ivMenu.setOnClickListener {
            showMenuOptions()
        }

        setupBottomNavigation()
    }

    private fun setupStoriesRecyclerView() {
        stories = mutableListOf()

        storiesAdapter = StoriesAdapter(stories) { position ->
            selectMediaForStory(position)
        }

        rvStories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvStories.adapter = storiesAdapter
    }

    private fun loadStories() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("/stories/$userId")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stories = mutableListOf<Story>()
                for (storySnapshot in snapshot.children) {
                    val story = storySnapshot.getValue(Story::class.java)
                    story?.let { stories.add(it) }
                }
                // Update your RecyclerView with the stories list
                storiesAdapter.updateStories(stories)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load stories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupChatListRecyclerView() {
        rvChatList.layoutManager = LinearLayoutManager(this)
        chatListAdapter = ChatAdapter(emptyList())
        rvChatList.adapter = chatListAdapter
        fetchChatsFromFirebase()
    }

    private fun fetchChatsFromFirebase() {
        firestore.collection("chats")
            .get()
            .addOnSuccessListener { querySnapshot ->
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
                R.id.navigation_search -> true
                R.id.navigation_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
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

            if (selectedMediaUri != null) {
                uploadStoryMedia(selectedMediaUri)
            }
        }
    }

    private fun uploadStoryMedia(selectedMediaUri: Uri) {
        // Generate a unique filename for the media
        val fileName = UUID.randomUUID().toString()
        val mediaRef = storageReference.child("stories/$fileName")

        // Upload the media to Firebase Storage
        mediaRef.putFile(selectedMediaUri)
            .addOnSuccessListener { taskSnapshot ->
                // Retrieve the download URL
                mediaRef.downloadUrl.addOnSuccessListener { uri ->
                    val newStory = Story("New Story", uri.toString())
                    addStoryToFirestore(newStory)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload story: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addStoryToFirestore(newStory: Story) {
        // Add the new story to Firestore
        firestore.collection("stories")
            .add(newStory)
            .addOnSuccessListener { documentReference ->
                addStory(newStory)
                Toast.makeText(this, "Story added successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add story: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addStory(newStory: Story) {
        stories.add(newStory)
        rvStories.adapter?.notifyItemInserted(stories.size - 1)
    }

    private fun showMenuOptions() {
        Toast.makeText(this, "Menu clicked!", Toast.LENGTH_SHORT).show()
    }
}