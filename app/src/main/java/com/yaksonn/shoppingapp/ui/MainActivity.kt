package com.yaksonn.shoppingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}