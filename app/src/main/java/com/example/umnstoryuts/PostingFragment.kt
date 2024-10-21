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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            if (content.isNotEmpty() && selectedImageUri != null) {
                uploadImage(content) // Start image upload
            } else {
                Toast.makeText(requireContext(), "Please add content and select an image!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Open the image chooser
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    // Handle image selection result
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageView.setImageURI(selectedImageUri)
            }
        }

    // Upload the image to Firebase Storage
    private fun uploadImage(content: String) {
        // Disable interactions and show loading
        setLoading(true)

        val postId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("posts/$postId.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        savePost(content, downloadUrl.toString(), postId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to upload image", exception)
                    setLoading(false) // Stop loading on failure
                    Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Save the post to Firestore
    private fun savePost(content: String, imageUrl: String, postId: String) {
        val post = mapOf(
            "id" to postId,
            "content" to content,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("posts").document(postId)
            .set(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post submitted!", Toast.LENGTH_SHORT).show()
                setLoading(false)

                // Update BottomNavigationView to highlight Home tab
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.nav_home

                // Navigate back to HomeFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to submit post!", Toast.LENGTH_SHORT).show()
            }
    }


    // Method to navigate back to HomeFragment
    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }


    // Show or hide the loading indicator
    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            postButton.isEnabled = false
            selectImageButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            postButton.isEnabled = true
            selectImageButton.isEnabled = true
        }
    }
}
