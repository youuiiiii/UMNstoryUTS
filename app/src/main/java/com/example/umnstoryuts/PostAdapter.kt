package com.example.umnstoryuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(
    private val posts: List<Post>,
    private val onLikeClicked: (Post, Int) -> Unit,
    private val onPinClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImageView: ImageView = view.findViewById(R.id.postImageView)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val likesTextView: TextView = view.findViewById(R.id.likesTextView)
        val pinButton: ImageView = view.findViewById(R.id.pinButton)
        val postContentTextView: TextView = view.findViewById(R.id.postContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.postContentTextView.text = post.content
        holder.likesTextView.text = "${post.likes} Likes"
        holder.likeButton.setImageResource(if (post.isLiked) R.drawable.baseline_thumb_up_24 else R.drawable.baseline_thumb_up_off_alt_24)
        holder.pinButton.setImageResource(if (post.pinned) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24)

        Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.postImageView)

        holder.likeButton.setOnClickListener {
            onLikeClicked(post, position)
        }

        holder.pinButton.setOnClickListener {
            onPinClicked(post)
        }
    }

    override fun getItemCount(): Int = posts.size
}
