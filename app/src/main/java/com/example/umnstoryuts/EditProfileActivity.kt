package com.example.umnstoryuts

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editStudentNumber: EditText
    private lateinit var saveProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editName = findViewById(R.id.editName)
        editStudentNumber = findViewById(R.id.editStudentNumber)
        saveProfileButton = findViewById(R.id.saveProfileButton)

        loadUserData()

        saveProfileButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("users").document(it.uid)
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: it.displayName ?: "Name"
                        val studentNumber = document.getString("studentNumber") ?: "Student Number"

                        editName.setText(name)
                        editStudentNumber.setText(studentNumber)
                    } else {
                        editName.setText(it.displayName ?: "Name")
                        editStudentNumber.setText("Student Number")
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
                    editName.setText(it.displayName ?: "Name")
                    editStudentNumber.setText("Student Number")
                }
        } ?: run {
            editName.setText("Name")
            editStudentNumber.setText("Student Number")
        }
    }


    private fun saveProfileChanges() {
        val newName = editName.text.toString().trim()
        val newStudentNumber = editStudentNumber.text.toString().trim()

        if (newName.isEmpty() || newStudentNumber.isEmpty()) {
            Toast.makeText(this, "Name and student number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updates = hashMapOf<String, Any>(
                        "name" to newName,
                        "studentNumber" to newStudentNumber
                    )
                    FirebaseFirestore.getInstance().collection("users").document(it.uid)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish() // Return to the profile page
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Failed to update Firebase profile", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
    }

}
