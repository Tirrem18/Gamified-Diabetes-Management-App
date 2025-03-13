package com.b1097780.glucohub

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseHelper(context: Context) {

    private var auth: FirebaseAuth

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)  // ✅ Only initialize if not already initialized
                Log.d("FirebaseHelper", "✅ Firebase Initialized in FirebaseHelper")
            }

            auth = FirebaseAuth.getInstance() // Now safe to get FirebaseAuth instance
            Log.d("FirebaseHelper", "✅ FirebaseHelper Initialized Successfully")

        } catch (e: Exception) {
            Log.e("FirebaseHelper", "❌ Firebase Initialization Failed: ${e.message}")
            throw RuntimeException("Firebase initialization failed!", e)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signInTestUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseHelper", "🔥 Login successful! User: ${auth.currentUser?.email}")
                } else {
                    Log.e("FirebaseHelper", "❌ Login failed: ${task.exception?.message}")
                }
            }
    }

    fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseHelper", "🔥 Anonymous login successful! User ID: ${auth.currentUser?.uid}")
                } else {
                    Log.e("FirebaseHelper", "❌ Anonymous login failed: ${task.exception?.message}")
                }
            }
    }


}
