package com.b1097780.glucohub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isSignUpMode = false // Track if in sign-up mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Enable Back Button in the ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val selectionLayout = findViewById<LinearLayout>(R.id.selectionLayout)
        val loginRegisterForm = findViewById<LinearLayout>(R.id.loginRegisterForm)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val showLoginButton = findViewById<Button>(R.id.showLoginButton)
        val showRegisterButton = findViewById<Button>(R.id.showRegisterButton)

        // Show Login Form
        showLoginButton.setOnClickListener {
            isSignUpMode = false
            usernameEditText.visibility = View.GONE
            registerButton.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
            selectionLayout.visibility = View.GONE
            loginRegisterForm.visibility = View.VISIBLE
        }

        // Show Sign Up Form
        showRegisterButton.setOnClickListener {
            isSignUpMode = true
            usernameEditText.visibility = View.VISIBLE
            registerButton.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
            selectionLayout.visibility = View.GONE
            loginRegisterForm.visibility = View.VISIBLE
        }

        // Login User
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Register User
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.length >= 6) {
                registerUser(username, email, password)
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Login Failed: ${task.exception?.message}")
                }
            }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val userData = mapOf(
                        "username" to username,
                        "email" to email,
                        "created_at" to System.currentTimeMillis()
                    )

                    if (uid != null) {
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                loginUser(email, password)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Handle the Back Button
    override fun onSupportNavigateUp(): Boolean {
        recreate() // Refreshes the LoginActivity instead of navigating away
        return true
    }



}
