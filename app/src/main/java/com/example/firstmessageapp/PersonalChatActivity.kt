package com.example.firstmessageapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference

class PersonalChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: PersonalChatAdapter // Corrected this line
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var chatUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        messagesList = ArrayList()

        chatUserId = intent.getStringExtra("userId")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        chatAdapter = PersonalChatAdapter(messagesList) // Corrected this line
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
    }

    private fun sendMessage(messageText: String) {
        val message = Message(currentUserId, messageText)
        val chatId = getChatId(currentUserId, chatUserId)
        database.child("chats").child(chatId).push().setValue(message)
        messageEditText.text.clear() // Clear the message box after sending
    }

    private fun listenForMessages() {
        val chatId = getChatId(currentUserId, chatUserId)
        database.child("chats").child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messagesList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalChatActivity, "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
    }
}