package com.b1097780.glucohub.ui.login

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        Log.d("FirebaseHelper", "✅ FirebaseHelper Initialized Successfully")
    }

    // 🔹 Register a New User
    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userData = mapOf(
                            "email" to email,
                            "created_at" to System.currentTimeMillis()
                        )
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("FirebaseHelper", "✅ User registered & added to Firestore")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseHelper", "❌ Failed to add user to Firestore: ${e.message}")
                                onComplete(false, e.message)
                            }
                    }
                } else {
                    Log.e("FirebaseHelper", "❌ Registration failed: ${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // 🔹 Log In User
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseHelper", "✅ User logged in: ${auth.currentUser?.email}")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseHelper", "❌ Login failed: ${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }
}
