package com.example.locbatt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

@Suppress("DEPRECATION")
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val nav = findViewById<BottomNavigationView>(R.id.nav5)
        nav.setOnNavigationItemReselectedListener { item->
            when(item.itemId){
                R.id.backHome->{
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                }
            }
        }
    }
}