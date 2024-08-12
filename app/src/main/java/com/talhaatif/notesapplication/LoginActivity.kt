package com.talhaatif.notesapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import  com.talhaatif.notesapplication.firebase.Util
import androidx.appcompat.app.AppCompatActivity
import com.talhaatif.notesapplication.databinding.ActivityLoginBinding
import com.talhaatif.notesapplication.firebase.Variables.Companion.auth
import com.talhaatif.notesapplication.firebase.Variables.Companion.displayErrorMessage
import com.talhaatif.notesapplication.firebase.Variables.Companion.isEmailValid


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialog: ProgressDialog
    private val utils = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")

        binding.login.setOnClickListener {
            if (binding.email.text.toString().isEmpty() || binding.password.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(binding.email.text.toString())) {
                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show()
            } else {
                login()
            }
        }


        binding.signupTv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }



    }

    private fun login() {
        progressDialog.show()
        auth.signInWithEmailAndPassword(binding.email.text.toString(),
            binding.password.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        utils.saveLocalData(this, "uid", it.uid)
                        utils.saveLocalData(this, "auth", "true")
                    }
                    progressDialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    progressDialog.dismiss()
                    val error = task.exception?.message
                    error?.let { displayErrorMessage(it,this) }
                }
            }
    }



}