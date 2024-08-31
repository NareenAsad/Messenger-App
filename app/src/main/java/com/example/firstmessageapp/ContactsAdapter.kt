package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ContactsAdapter(
    private val contacts: MutableList<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user = contacts[position]
        holder.bind(user)
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<User>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contactImageView: ImageView = itemView.findViewById(R.id.contactImageView)
        private val contactName: TextView = itemView.findViewById(R.id.contactName)
        private val contactStatus: TextView = itemView.findViewById(R.id.contactStatus)

        fun bind(user: User) {
            contactName.text = user.name

            // Load image using Glide
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(contactImageView)

            // Display online status or last seen
            contactStatus.text = if (user.isOnline) {
                "Online"
            } else {
                "Last seen: ${user.lastSeen ?: "N/A"}"
            }

            itemView.setOnClickListener { onItemClick(user) }
        }
    }
}