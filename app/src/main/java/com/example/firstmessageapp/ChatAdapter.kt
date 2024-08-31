package com.example.firstmessageapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private var chats: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

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

        with(holder) {
            name.text = chat.name ?: "Unknown"
            lastMessage.text = chat.lastMessage ?: "No messages"
            timestamp.text = chat.timestamp ?: "No timestamp"
            unreadCount.text = if (chat.unreadCount > 0) chat.unreadCount.toString() else ""

            Picasso.get()
                .load(chat.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder) // Show placeholder while loading
                .error(R.drawable.profile_placeholder) // Show placeholder on error
                .into(profileImage)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, PersonalChatActivity::class.java).apply {
                    putExtra("name", chat.name)
                    putExtra("uid", chat.userId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateChats(newChats: List<Chat>) {
        val diffResult = DiffUtil.calculateDiff(ChatDiffCallback(chats, newChats))
        chats = newChats
        diffResult.dispatchUpdatesTo(this)
    }

    private class ChatDiffCallback(
        private val oldChats: List<Chat>,
        private val newChats: List<Chat>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldChats.size

        override fun getNewListSize(): Int = newChats.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldChats[oldItemPosition].userId == newChats[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldChat = oldChats[oldItemPosition]
            val newChat = newChats[newItemPosition]
            return oldChat == newChat
        }
    }
}