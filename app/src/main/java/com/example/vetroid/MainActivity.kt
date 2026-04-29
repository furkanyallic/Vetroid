package com.example.vetroid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom Navigation View setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, HomeFragment())
                        commit()
                    }
                    true
                }
                R.id.nav_macros -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, MacrosFragment())
                        commit()
                    }
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, SettingsFragment())
                        commit()
                    }
                    true
                }
                else -> false
            }
        }

        // Set default selection to home
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }
}





