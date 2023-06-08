package com.example.locbatt

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.locbatt.databinding.ActivitySplashBinding

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)

        Handler().postDelayed({
            val set=AnimatorInflater
                .loadAnimator(this@SplashActivity,R.animator.logo_animator) as AnimatorSet
            set.setTarget(binding.splashLogo)
            set.start()
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                finish()
            },600)
        },4000)


    }
}