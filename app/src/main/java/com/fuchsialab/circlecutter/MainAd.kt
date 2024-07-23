package com.fuchsialab.circlecutter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fuchsialab.circlecutter.Admob.loadInter

open class MainAd : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInter(this@MainAd)
    }
}