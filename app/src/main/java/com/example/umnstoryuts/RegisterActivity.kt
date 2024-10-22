package com.example.umnstoryuts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val name = findViewById<EditText>(R.id.editTextName)
        val studentNumber = findViewById<EditText>(R.id.editTextStudentNumber)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val confirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val nameText = name.text.toString().trim()
            val studentNumberText = studentNumber.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()

            if (passwordText == confirmPasswordText) {
                register(nameText, studentNumberText, emailText, passwordText)
            } else {
                Toast.makeText(baseContext, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun register(name: String, studentNumber: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let { user ->
                        val userData = hashMapOf(
                            "name" to name,
                            "studentNumber" to studentNumber,
                            "email" to email
                        )
                        firestore.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext, "Registration successful.",
                                    Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(baseContext, "Failed to save user data.",
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
