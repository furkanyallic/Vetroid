package com.example.vetroid

import android.Manifest
import android.app.AlertDialog
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    // 1. Tehlikeli izinler (konum, SMS, kişiler, bluetooth)
    private val dangerousLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { requestBackgroundLocationIfNeeded() }

    // 2. Arka plan konum (ayrı istenecek — Android politikası)
    private val bgLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { showSpecialPermissionsIfNeeded() }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("vetroid_prefs", MODE_PRIVATE)
        val savedMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(savedMode)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home    -> HomeFragment()
                R.id.nav_macros  -> MacrosFragment()
                R.id.nav_history -> HistoryFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            true
        }
        if (savedInstanceState == null) bottomNav.selectedItemId = R.id.nav_home

        checkAndRequestDangerousPermissions()
        startAppMonitorIfNeeded()
        startShakeDetectorIfNeeded()
    }

    private fun checkAndRequestDangerousPermissions() {
        val toRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            dangerousLauncher.launch(toRequest.toTypedArray())
        } else {
            requestBackgroundLocationIfNeeded()
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Arka Plan Konum İzni")
                .setMessage(
                    "Konum tabanlı makrolar (örn. eve girerken Wi-Fi aç) telefonun ekranı kapalıyken de " +
                    "çalışabilmesi için 'Her zaman izin ver' seçeneğini seçmeniz gerekiyor."
                )
                .setPositiveButton("İzin Ver") { _, _ ->
                    bgLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .setNegativeButton("Atla") { _, _ -> showSpecialPermissionsIfNeeded() }
                .setCancelable(false)
                .show()
        } else {
            showSpecialPermissionsIfNeeded()
        }
    }

    private fun showSpecialPermissionsIfNeeded() {
        // Özel izinler ilk açılışta bir kez sorulur; sonraki açılışlarda atlanır
        if (prefs.getBoolean("special_permissions_prompted", false)) return
        prefs.edit().putBoolean("special_permissions_prompted", true).apply()

        // Eksik özel izinleri topla: (başlık, açıklama, ayar intent'i)
        val pending = mutableListOf<Triple<String, String, Intent>>()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            pending += Triple(
                "Sessiz Mod (Rahatsız Etme) İzni",
                "Telefonu sessize almak için 'Rahatsız Etme' erişimi gereklidir.\n\nAçılan ekranda Vetroid uygulamasını bulup etkinleştirin, sonra geri dönün.",
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            )
        }

        if (!Settings.System.canWrite(this)) {
            pending += Triple(
                "Parlaklık Kontrolü İzni",
                "Ekran parlaklığını otomatik değiştirmek için sistem ayarları yazma izni gereklidir.\n\nAçılan ekranda Vetroid'e izin verin.",
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                pending += Triple(
                    "Tam Zamanlı Alarm İzni",
                    "Saat tabanlı makroların tam dakikasında çalışması için zamanlama izni gereklidir.\n\nAçılan ekranda Vetroid'e izin verin.",
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                )
            }
        }

        showNextSpecialPermission(pending, 0)
    }

    private fun startAppMonitorIfNeeded() {
        lifecycleScope.launch {
            val db = com.example.vetroid.data.AppDatabase.getDatabase(this@MainActivity)
            val hasAppTriggers = db.triggerDao()
                .getActiveTriggersByType(AppTriggerSetupActivity.TRIGGER_TYPE).isNotEmpty()
            if (hasAppTriggers) {
                startService(Intent(this@MainActivity, AppMonitorService::class.java))
            }
        }
    }

    private fun startShakeDetectorIfNeeded() {
        lifecycleScope.launch {
            val db = com.example.vetroid.data.AppDatabase.getDatabase(this@MainActivity)
            val hasShakeTriggers = db.triggerDao()
                .getActiveTriggersByType(ShakeTriggerSetupActivity.TRIGGER_TYPE).isNotEmpty()
            if (hasShakeTriggers) {
                startService(Intent(this@MainActivity, ShakeDetectorService::class.java))
            }
        }
    }

    private fun showNextSpecialPermission(
        pending: List<Triple<String, String, Intent>>,
        index: Int
    ) {
        if (index >= pending.size) return
        val (title, message, settingsIntent) = pending[index]
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ayarlara Git") { _, _ ->
                startActivity(settingsIntent)
                showNextSpecialPermission(pending, index + 1)
            }
            .setNegativeButton("Atla") { _, _ ->
                showNextSpecialPermission(pending, index + 1)
            }
            .setCancelable(false)
            .show()
    }
}
