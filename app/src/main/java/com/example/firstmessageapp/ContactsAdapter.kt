package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImageView: ImageView = view.findViewById(R.id.contactImageView)
        val nameTextView: TextView = view.findViewById(R.id.contactName)
        val phoneTextView: TextView = view.findViewById(R.id.contactPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phoneNumber

        // Load profile image using Glide
        Glide.with(holder.itemView.context)
            .load(contact.profileImageUrl)
            .placeholder(R.drawable.profile_placeholder) // Ensure this drawable exists in your project
            .error(R.drawable.profile_placeholder) // Use the same placeholder for errors
            .circleCrop() // Make the image circular
            .into(holder.profileImageView)

        // Set item click listener
        holder.itemView.setOnClickListener { onContactClick(contact) }
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }
}