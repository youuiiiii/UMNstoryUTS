package com.example.umnstoryuts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var studentNumberTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        studentNumberTextView = view.findViewById(R.id.studentNumberTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("users").document(it.uid)
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameTextView.text = document.getString("name") ?: "No Name"
                        studentNumberTextView.text = document.getString("studentNumber") ?: "No Student Number"
                        emailTextView.text = it.email
                        Glide.with(this).load(it.photoUrl ?: R.drawable.baseline_person_24).into(profileImageView)
                    }
                }.addOnFailureListener {
                    nameTextView.text = "Failed to load"
                    studentNumberTextView.text = "Failed to load"
                }
        }
    }
}
