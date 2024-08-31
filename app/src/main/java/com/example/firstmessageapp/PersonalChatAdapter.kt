package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class PersonalChatAdapter(private val messageList: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.sent_msg_item_layout, parent, false)
            SentMessageViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.received_msg_item_layout, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_sent_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.sent_msg_time_stamp)

        fun bind(message: Message) {
            messageTextView.text = message.messageText
            timestampTextView.text = message.timestamp
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_received_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.rec_msg_time_stamp)

        fun bind(message: Message) {
            messageTextView.text = message.messageText
            timestampTextView.text = message.timestamp
        }
    }
}