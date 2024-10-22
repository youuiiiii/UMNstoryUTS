package com.example.umnstoryuts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val postList = mutableListOf<Post>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostAdapter(postList, onLikeClicked = { post, position ->
            incrementLikes(post, position)
        }, onPinClicked = { post ->
            togglePin(post)
        })
        recyclerView.adapter = adapter
        fetchPosts()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                postList.clear()
                for (document in result) {
                    try {
                        val post = document.toObject(Post::class.java).apply { id = document.id }
                        postList.add(post)
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error parsing post", e)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching posts", exception)
                Toast.makeText(requireContext(), "Failed to load posts.", Toast.LENGTH_LONG).show()
            }
    }

    private fun incrementLikes(post: Post, position: Int) {
        val newLikeCount = if (post.isLiked) post.likes - 1 else post.likes + 1
        post.likes = newLikeCount
        post.isLiked = !post.isLiked

        FirebaseFirestore.getInstance().collection("posts").document(post.id)
            .update(mapOf(
                "likes" to newLikeCount,
                "isLiked" to post.isLiked
            )).addOnCompleteListener {
                adapter.notifyItemChanged(position)
            }
    }

    private fun togglePin(post: Post) {
        val wasPinned = post.pinned
        post.pinned = !post.pinned
        if (wasPinned != post.pinned) {
            postList.sortWith(compareByDescending<Post> { it.pinned }.thenByDescending { it.timestamp })
            adapter.notifyDataSetChanged()
        }

        FirebaseFirestore.getInstance().collection("posts").document(post.id)
            .update("pinned", post.pinned)
    }
}
