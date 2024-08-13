package com.talhaatif.notesapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.google.firebase.FirebaseApp
import com.talhaatif.notesapplication.databinding.ActivitySplashScreenBinding
import com.talhaatif.notesapplication.firebase.Util


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val utils = Util()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.lottieAnimationView.animate().translationY(1500F).setDuration(1000).setStartDelay(3500)

        Handler().postDelayed({
            val authStatus = utils.getLocalData(this, "auth")
            val targetClass = if (authStatus == "true") MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this, targetClass))
            finish()
        }, 3000)
    }
}