package com.example.firstmessageapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.squareup.picasso.Picasso

class ChatListAdapter(private val chats: List<Chat>) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.imgProfile)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val lastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.tvTime)
        val unreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.name.text = chat.name
        holder.lastMessage.text = chat.lastMessage
        holder.timestamp.text = chat.timestamp
        holder.unreadCount.text = chat.unreadCount.toString()

        // Load profile image if available
        if (chat.profileImageUrl != null) {
            Picasso.get().load(chat.profileImageUrl).into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile_placeholder) // Fallback image
        }

        // Handle item click to navigate to PersonalChatActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PersonalChatActivity::class.java).apply {
                putExtra("name", chat.name)
                putExtra("uid", chat.uid)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chats.size
}