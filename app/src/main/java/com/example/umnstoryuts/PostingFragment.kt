package com.example.umnstoryuts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class PostingFragment : Fragment() {

    private lateinit var postContentEditText: EditText
    private lateinit var selectedImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var postButton: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_posting, container, false)

        // Initialize views
        postContentEditText = view.findViewById(R.id.inputPostContent)
        selectedImageView = view.findViewById(R.id.selectedImageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        postButton = view.findViewById(R.id.submitPostBtn)
        progressBar = view.findViewById(R.id.progressBar)

        // Handle image selection
        selectImageButton.setOnClickListener { openImageChooser() }

        // Handle post submission
        postButton.setOnClickListener {
            val content = postContentEditText.text.toString().trim()
            if (content.isNotEmpty() || selectedImageUri != null) {
                if (selectedImageUri != null) {
                    uploadImage(content)
                } else {
                    postTextOnly(content)  // Post only text
                }
            } else {
                Toast.makeText(requireContext(), "Please add some content or select an image!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImage(content: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        setLoading(true)

        val postId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("posts/$postId.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val newPost = Post(
                            id = postId,
                            uid = user.uid,
                            content = content,
                            imageUrl = downloadUrl.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                        addPost(newPost)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to upload image", exception)
                    setLoading(false)
                    Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            setLoading(false)
            Toast.makeText(requireContext(), "Please select an image first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postTextOnly(content: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        setLoading(true)

        val postId = UUID.randomUUID().toString()
        val newPost = Post(
            id = postId,
            uid = user.uid,
            content = content,
            imageUrl = null.toString(),
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("posts").document(postId)
            .set(newPost)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post submitted!", Toast.LENGTH_SHORT).show()
                setLoading(false)
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to submit post", e)
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to submit post!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addPost(post: Post) {
        firestore.collection("posts").document(post.id)
            .set(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post submitted!", Toast.LENGTH_SHORT).show()
                setLoading(false)
                navigateToHome()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to submit post", exception)
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to submit post!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        postButton.isEnabled = !isLoading
        selectImageButton.isEnabled = !isLoading
    }
}
