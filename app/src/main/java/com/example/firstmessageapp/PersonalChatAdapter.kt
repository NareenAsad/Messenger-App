package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class PersonalChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<PersonalChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message) {
            messageTextView.text = message.messageText

            // Update layout parameters to align messages properly
            val params = messageTextView.layoutParams as RelativeLayout.LayoutParams
            if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                params.addRule(RelativeLayout.ALIGN_PARENT_END)
                messageTextView.background = itemView.context.getDrawable(R.drawable.sent_message_background) // Customize background for sent messages
            } else {
                params.addRule(RelativeLayout.ALIGN_PARENT_START)
                messageTextView.background = itemView.context.getDrawable(R.drawable.received_message_background) // Customize background for received messages
            }
            messageTextView.layoutParams = params
        }
    }

}