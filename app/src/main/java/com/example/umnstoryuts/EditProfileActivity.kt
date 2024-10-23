package com.example.umnstoryuts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editStudentNumber: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var saveProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editName = findViewById(R.id.editName)
        editStudentNumber = findViewById(R.id.editStudentNumber)
        profileImageView = findViewById(R.id.profileImageView)
        saveProfileButton = findViewById(R.id.saveProfileButton)

        loadUserData()

        profileImageView.setOnClickListener {
            openImageSelector()
        }

        saveProfileButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun openImageSelector() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001) // Arbitrary request code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data as Uri
            profileImageView.setImageURI(imageUri)
            uploadImageToFirebase(imageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/${user.uid}.jpg")
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("profilePictureUrl", photoUrl)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile image updated.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile in Firestore.", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .get().addOnSuccessListener { document ->
                if (document.exists()) {
                    editName.setText(document.getString("name"))
                    editStudentNumber.setText(document.getString("studentNumber"))
                    document.getString("profilePictureUrl")?.let { imageUrl ->
                        Glide.with(this).load(imageUrl).into(profileImageView)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileChanges() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val newName = editName.text.toString().trim()
        val newStudentNumber = editStudentNumber.text.toString().trim()

        if (newName.isEmpty() || newStudentNumber.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "name" to newName,
            "studentNumber" to newStudentNumber
        )
        FirebaseFirestore.getInstance().collection("users").document(user.uid).update(updates)
            .addOnSuccessListener {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(newName).build())
                Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }
}
