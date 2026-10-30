package com.udb.mundoclima.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udb.mundoclima.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)
        lifecycleScope.launch {
            delay(1500)
            startActivity(Intent(this@SplashActivity, RegionsActivity::class.java))
            finish()
        }
    }
}
