package com.fuchsialab.circlecutter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Launcher :  MainAd() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launcher)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideNavigationbar()

        Handler().postDelayed({
            val i = Intent(
                this@Launcher,
                HomeActivity::class.java
            )
            startActivity(i)
            finish()
        }, 800)


    }
    private fun hideNavigationbar() {
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}