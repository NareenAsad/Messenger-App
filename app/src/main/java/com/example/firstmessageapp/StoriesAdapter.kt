package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoriesAdapter(
    private val stories: MutableList<Story>,
    private val onStoryClick: (position: Int) -> Unit
) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View, private val onStoryClick: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val storyImage: CircleImageView = itemView.findViewById(R.id.imgStory)
        val storyLabel: TextView = itemView.findViewById(R.id.tvStoryLabel)

        init {
            itemView.setOnClickListener {
                onStoryClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view, onStoryClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.storyLabel.text = story.title
        Picasso.get()
            .load(story.imageUrl)
            .placeholder(R.drawable.profile_placeholder) // Optional placeholder image
            .into(holder.storyImage)
    }

    fun updateStories(newStories: List<Story>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = stories.size
}