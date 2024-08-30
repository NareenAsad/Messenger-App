package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ContactsAdapter(
    private var contacts: List<User>,
    private val onContactClick: (User) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    fun updateContacts(newContacts: List<User>) {
        val diffResult = DiffUtil.calculateDiff(UserDiffCallback(contacts, newContacts))
        contacts = newContacts
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user = contacts[position]
        holder.bind(user)
        holder.itemView.setOnClickListener { onContactClick(user) }
    }

    override fun getItemCount(): Int = contacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contactName)
        private val phoneTextView: TextView = itemView.findViewById(R.id.contactPhone)
        private val profileImageView: ImageView = itemView.findViewById(R.id.contactImageView)

        fun bind(user: User) {
            nameTextView.text = user.name
            phoneTextView.text = user.phoneNumber
            Glide.with(itemView.context).load(user.profilePictureUrl).into(profileImageView)
        }
    }
}