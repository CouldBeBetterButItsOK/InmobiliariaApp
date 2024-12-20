package com.teknos.m8uf2.fxsane.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.ActivityMainBinding
import com.teknos.m8uf2.fxsane.fragment.LogInFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.log_in_fragment_container, LogInFragment())
                .commitNow()
        }
    }
}