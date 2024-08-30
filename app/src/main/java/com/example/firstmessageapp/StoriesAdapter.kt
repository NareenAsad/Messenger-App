package com.example.firstmessageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoriesAdapter(private val stories: List<Story>) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: CircleImageView = itemView.findViewById(R.id.imgStory)
        val storyLabel: TextView = itemView.findViewById(R.id.tvStoryLabel)

        init {
            itemView.setOnClickListener {
                // Trigger the image/video selection process
                val context = itemView.context
                if (context is ChatActivity) {
                    context.selectMediaForStory(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.storyLabel.text = story.title
        Picasso.get()
            .load(story.imageUrl)
            .placeholder(R.drawable.profile_placeholder) // Optional placeholder image
            .into(holder.storyImage)
    }

    override fun getItemCount(): Int = stories.size
}
