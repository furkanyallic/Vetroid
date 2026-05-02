package com.example.vetroid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class TriggersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_triggers)


        findViewById<MaterialCardView>(R.id.cardKonum).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardTarihSaat).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardPil).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardUygulamalar).setOnClickListener { }

    }
}