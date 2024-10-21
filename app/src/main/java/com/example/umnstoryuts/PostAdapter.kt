package com.example.umnstoryuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImageView: ImageView = view.findViewById(R.id.postImageView)
        val postContentTextView: TextView = view.findViewById(R.id.postContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Bind post content
        holder.postContentTextView.text = post.content

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
//            .placeholder(R.drawable.placeholder)
//            .error(R.drawable.error_image)
            .into(holder.postImageView)
    }

    override fun getItemCount(): Int = postList.size
}
