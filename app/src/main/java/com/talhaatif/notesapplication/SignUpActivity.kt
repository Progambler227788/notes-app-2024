package com.talhaatif.notesapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.SetOptions
import com.talhaatif.notesapplication.databinding.ActivitySignUpBinding
import com.talhaatif.notesapplication.firebase.Variables

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")

        binding.signup.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.cpassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                signUpUser(username, email, password)
            }
        }
        binding.loginTv.setOnClickListener{
            binding.loading.visibility = android.view.View.GONE
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username is required"
            return false
        } else {
            binding.usernameLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Invalid email format"
            return false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.passwordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.cpasswordLayout.error = "Confirm password is required"
            return false
        } else if (confirmPassword != password) {
            binding.cpasswordLayout.error = "Passwords do not match"
            return false
        } else {
            binding.cpasswordLayout.error = null
        }

        return true
    }

    private fun signUpUser(username: String, email: String, password: String) {
        progressDialog.show()

        Variables.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val uid = Variables.auth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "useremail" to email
                        )

                        Variables.db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                // Navigate to the next activity
                                progressDialog.dismiss()

                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Variables.displayErrorMessage(" ${e.message}", this)
                                progressDialog.dismiss()
                            }
                    }
                } else {
                    progressDialog.dismiss()
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Variables.displayErrorMessage("This email address is already in use.", this)
                    } catch (e: Exception) {
                        Variables.displayErrorMessage("Authentication failed: ${e.message}", this)
                    }
                }
            }
    }
}
