package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddMacroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_macro)

        // 1. Pencere Ayarları (Padding)
        val mainLayout = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Toolbar Kurulumu (Geri tuşu için burası kritik)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // Bu Toolbar'ı ana aksiyon barı yap

        // Geri tuşunun tıklama olayını dinle
        toolbar.setNavigationOnClickListener {
            // Kullanıcıyı bir önceki ekrana döndürür
            onBackPressedDispatcher.onBackPressed()
        }

        // 3. Diğer Buton Tanımlamaları
        val btnAddTrigger = findViewById<ImageView>(R.id.btnAddTrigger)
        val btnAddAction = findViewById<ImageView>(R.id.btnAddAction)
        val btnAddConstraint = findViewById<ImageView>(R.id.btnAddConstraint)

        btnAddTrigger.setOnClickListener {
            startActivity(Intent(this, TriggersActivity::class.java))
        }

        btnAddAction.setOnClickListener {
            startActivity(Intent(this, ActionsActivity::class.java))
        }

        btnAddConstraint.setOnClickListener {
            startActivity(Intent(this, ConstraintsActivity::class.java))
        }
    }
}