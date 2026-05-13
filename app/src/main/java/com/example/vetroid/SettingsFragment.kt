package com.example.vetroid

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLight = view.findViewById<MaterialButton>(R.id.btn_light_theme)
        val btnDark = view.findViewById<MaterialButton>(R.id.btn_dark_theme)

        val prefs = requireContext().getSharedPreferences("vetroid_prefs", Context.MODE_PRIVATE)
        val currentMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        updateButtonStates(btnLight, btnDark, currentMode)

        btnLight.setOnClickListener {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO, btnLight, btnDark, prefs)
        }

        btnDark.setOnClickListener {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES, btnLight, btnDark, prefs)
        }
    }

    private fun applyTheme(mode: Int, btnLight: MaterialButton, btnDark: MaterialButton, prefs: SharedPreferences) {
        prefs.edit().putInt("night_mode", mode).apply()
        updateButtonStates(btnLight, btnDark, mode)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun updateButtonStates(btnLight: MaterialButton, btnDark: MaterialButton, mode: Int) {
        val isLight = mode == AppCompatDelegate.MODE_NIGHT_NO
        val isDark = mode == AppCompatDelegate.MODE_NIGHT_YES

        btnLight.alpha = if (isLight) 1f else 0.5f
        btnDark.alpha = if (isDark) 1f else 0.5f

        btnLight.strokeWidth = if (isLight) dpToPx(3) else dpToPx(1)
        btnDark.strokeWidth = if (isDark) dpToPx(3) else dpToPx(1)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
