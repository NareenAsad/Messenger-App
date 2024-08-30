package com.example.firstmessageapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference

class PersonalChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var backButton: ImageView
    private lateinit var chatUsername: TextView
    private lateinit var icSearch: ImageView
    private lateinit var icMenu: ImageView

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_chat)

        // Retrieve data from intent
        val name = intent.getStringExtra("contactName")
        val receiverUid = intent.getStringExtra("contactId")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        // Initialize Firebase Database
        mDbRef = FirebaseDatabase.getInstance().reference

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        backButton = findViewById(R.id.backButton)
        chatUsername = findViewById(R.id.chat_username)
        icSearch = findViewById(R.id.icSearch)
        icMenu = findViewById(R.id.icMenu)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        // Set up the RecyclerView
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // Set the username in the header
        chatUsername.text = name

        // Handle back button click
        backButton.setOnClickListener {
            finish()
        }

        // Handle search and menu button clicks
        icSearch.setOnClickListener { /* Implement search functionality */ }
        icMenu.setOnClickListener { /* Implement menu functionality */ }

        // Listen for new messages
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })

        // Send a new message
        sendButton.setOnClickListener {
            val message = messageBox.text.toString().trim()
            if (message.isNotEmpty()) {
                val messageObject = Message(message, senderUid!!)

                // Push message to the sender's chat room
                mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject)
                    .addOnSuccessListener {
                        // Push the same message to the receiver's chat room
                        mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }
                messageBox.setText("")
            }
        }
    }
}