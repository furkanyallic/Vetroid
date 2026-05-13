package com.example.vetroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.GeofenceParams
import com.example.vetroid.data.Trigger
import com.example.vetroid.databinding.ActivityGeofenceSetupBinding
import com.example.vetroid.trigger.GeofenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.UUID

class GeofenceSetupActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGeofenceSetupBinding
    private var googleMap: GoogleMap? = null
    private var selectedCircle: Circle? = null
    private var selectedLatLng: LatLng? = null

    private lateinit var database: AppDatabase
    private lateinit var geofenceManager: GeofenceManager
    private var scenarioId: Long = 0
    private var triggerId: Long = 0

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableMyLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableMyLocation()
            }
            else -> {
                Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        geofenceManager = GeofenceManager(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        triggerId = intent.getLongExtra("trigger_id", 0)
        android.util.Log.d("GeofenceSetupActivity", "scenarioId: $scenarioId, triggerId: $triggerId")

        setupToolbar()
        setupMap()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupListeners() {
        binding.sliderRadius.addOnChangeListener { _, value, _ ->
            binding.tvRadiusValue.text = "${value.toInt()}m"
            selectedCircle?.radius = value.toDouble()
        }

        binding.btnSave.setOnClickListener {
            saveGeofence()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Default location (Türkiye - Ankara)
        val defaultLocation = LatLng(39.9334, 32.8597)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Map click listener
        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            updateMapCircle(latLng)
            binding.tvCoordinates.text = "Lat: %.4f, Lng: %.4f".format(latLng.latitude, latLng.longitude)
            binding.btnSave.isEnabled = true
        }
    }

    private fun updateMapCircle(latLng: LatLng) {
        selectedCircle?.remove()

        val radius = binding.sliderRadius.value.toDouble()
        selectedCircle = googleMap?.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(ContextCompat.getColor(this, R.color.teal_700))
                .fillColor(ContextCompat.getColor(this, R.color.teal_200))
        )
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            // Handle exception
        }
    }

    private fun saveGeofence() {
        val latLng = selectedLatLng ?: run {
            Toast.makeText(this, "Lütfen haritadan bir konum seçin", Toast.LENGTH_SHORT).show()
            return
        }

        val transitionType = when (binding.rgTransition.checkedRadioButtonId) {
            R.id.rb_enter -> "ENTER"
            R.id.rb_exit -> "EXIT"
            R.id.rb_both -> "BOTH"
            else -> "ENTER"
        }

        val radius = binding.sliderRadius.value

        val params = GeofenceParams(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            radius = radius,
            transitionType = transitionType
        )

        lifecycleScope.launch {
            try {
                if (triggerId > 0) {
                    // UPDATE - mevcut trigger'ı güncelle
                    val existingTrigger = database.triggerDao().getTriggerById(triggerId)
                    existingTrigger?.let {
                        val updated = it.copy(
                            params = params.toJson(),
                            isActive = true
                        )
                        database.triggerDao().update(updated)
                        Log.d("GeofenceSetupActivity", "✅ Trigger güncellendi: $triggerId")
                    }
                } else {
                    // INSERT - yeni trigger oluştur
                    val trigger = Trigger(
                        scenarioId = scenarioId,
                        type = "GEOFENCE",
                        params = params.toJson(),
                        isActive = true
                    )
                    triggerId = database.triggerDao().insert(trigger)
                    Log.d("GeofenceSetupActivity", "✅ Yeni Trigger oluşturuldu: $triggerId")
                }

                // Geofence sisteme ekle
                geofenceManager.addGeofence(
                    geofenceId = "geofence_$triggerId",
                    params = params,
                    onSuccess = {
                        Log.d("GeofenceSetupActivity", "✅ Geofence sistem tarafına eklendi")
                        runOnUiThread {
                            Toast.makeText(this@GeofenceSetupActivity, "Geofence kaydedildi", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    },
                    onFailure = { e ->
                        Log.e("GeofenceSetupActivity", "❌ Geofence eklenirken hata: ${e.message}", e)
                        runOnUiThread {
                            Toast.makeText(this@GeofenceSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("GeofenceSetupActivity", "❌ Database hatası: ${e.message}", e)
                Toast.makeText(this@GeofenceSetupActivity, "Database hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }
}