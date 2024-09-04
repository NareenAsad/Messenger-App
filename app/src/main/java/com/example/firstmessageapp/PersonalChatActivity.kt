package com.example.firstmessageapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PersonalChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: PersonalChatAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var chatUserId: String
    private lateinit var chatUsernameTextView: TextView

    private var senderRoom: String? = null
    private var receiverRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        chatUsernameTextView = findViewById(R.id.chat_username)
        messagesList = ArrayList()

        chatUserId = intent.getStringExtra("userId") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Setup sender and receiver rooms for chat
        senderRoom = "$currentUserId-$chatUserId"
        receiverRoom = "$chatUserId-$currentUserId"

        chatAdapter = PersonalChatAdapter(messagesList, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        database = FirebaseDatabase.getInstance().reference

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        listenForMessages()
        loadChatUserDetails()
    }

    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis().toString()
        val message = Message(currentUserId, messageText, timestamp)

        // Store message in the sender's room
        database.child("chats").child(senderRoom!!).push().setValue(message).addOnSuccessListener {
            // Also store the message in the receiver's room
            database.child("chats").child(receiverRoom!!).push().setValue(message)

            // Update chat metadata for the sender
            updateChatMetadata(currentUserId, chatUserId, messageText, timestamp)

            // Update chat metadata for the receiver
            updateChatMetadata(chatUserId, currentUserId, messageText, timestamp)
        }
        messageEditText.text.clear()
    }

    private fun updateChatMetadata(userId: String, otherUserId: String, lastMessage: String, timestamp: String) {
        val chatMetadata = Chat(
            name = otherUserId, // You might want to store the other user's name here
            lastMessage = lastMessage,
            timestamp = timestamp,
            user1Name = userId,
            user2Name = otherUserId
        )
        database.child("ChatMetadata").child(userId).child(otherUserId).setValue(chatMetadata)
    }

    private fun listenForMessages() {
        database.child("chats").child(senderRoom!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    message?.let { messagesList.add(it) }
                }
                chatAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messagesList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalChatActivity, "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadChatUserDetails() {
        database.child("users").child(chatUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    chatUsernameTextView.text = it.name
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalChatActivity, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}