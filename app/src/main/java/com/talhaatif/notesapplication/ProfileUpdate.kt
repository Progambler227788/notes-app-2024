package com.talhaatif.notesapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.talhaatif.notesapplication.databinding.ActivityProfileUpdateBinding
import com.talhaatif.notesapplication.firebase.Util
import com.talhaatif.notesapplication.firebase.Variables

class ProfileUpdate : AppCompatActivity() {

    private lateinit var binding: ActivityProfileUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCurrentUsername()

        // Set up click listeners
        binding.update.setOnClickListener {
            handleUpdateUsername()
        }

        binding.logout.setOnClickListener {
            handleLogout()
        }
    }
    private fun fetchCurrentUsername() {
        val uid = Util().getLocalData(this, "uid")
        if (uid.isNotEmpty()) {
            Variables.db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val currentUsername = document.getString("username")
                        binding.username.setText(currentUsername)
                    } else {
                        Toast.makeText(this, "No such user found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Variables.displayErrorMessage("Error fetching username: ${e.message}", this)
                }
        }
    }

    private fun handleUpdateUsername() {
        val username = binding.username.text.toString().trim()
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
        } else {
            val uid = Util().getLocalData(this, "uid")
            if (uid.isNotEmpty()) {
                // Create a map for Firestore update
                val userUpdates: MutableMap<String, Any> = hashMapOf("username" to username)
                Variables.db.collection("users").document(uid)
                    .update(userUpdates as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Variables.displayErrorMessage("Error updating username: ${e.message}", this)
                    }
            }
        }
    }

    private fun handleLogout() {
        Util().saveLocalData(this, "uid", "")
        Util().saveLocalData(this, "auth", "false")

        // Redirect to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
