package com.example.vetroid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.TimeParams
import com.example.vetroid.data.TimeSchedule
import com.example.vetroid.data.Trigger
import com.example.vetroid.databinding.ActivityTimeSetupBinding
import com.example.vetroid.trigger.TimeTriggerManager
import kotlinx.coroutines.launch
import java.util.Calendar

class TimeSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeSetupBinding
    private lateinit var database: AppDatabase
    private lateinit var timeTriggerManager: TimeTriggerManager
    private lateinit var prefs: SharedPreferences
    private var scenarioId: Long = 0
    private var triggerId: Long = 0

    // Tek seferlik seçim
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var selectedHour: Int = 18
    private var selectedMinute: Int = 0

    // Gün başına saatler (varsayılan 18:00)
    private var scheduleMap = mutableMapOf(
        1 to TimeSchedule(1, 18, 0),
        2 to TimeSchedule(2, 18, 0),
        3 to TimeSchedule(3, 18, 0),
        4 to TimeSchedule(4, 18, 0),
        5 to TimeSchedule(5, 18, 0),
        6 to TimeSchedule(6, 18, 0),
        7 to TimeSchedule(7, 18, 0)
    )

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_TIME_TRIGGER_ID = "current_time_trigger_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        timeTriggerManager = TimeTriggerManager(this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        scenarioId = intent.getLongExtra("scenario_id", 0)
        triggerId = intent.getLongExtra("trigger_id", 0)

        // Database'den mevcut TIME trigger'ı bul (SharedPreferences güvenilir değil!)
        if (triggerId == 0L && scenarioId > 0) {
            lifecycleScope.launch {
                val existingTrigger = database.triggerDao().getTriggerByScenarioAndType(scenarioId, "TIME")
                triggerId = existingTrigger?.id ?: 0
                Log.d("TimeSetupActivity", "Database'den trigger arandı, bulunan: $triggerId")
                if (triggerId > 0) {
                    loadExistingTrigger()
                }
            }
        } else if (triggerId > 0) {
            loadExistingTrigger()
        }

        Log.d("TimeSetupActivity", "scenarioId: $scenarioId, triggerId: $triggerId")

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        // Switch: Tek seferlik / Tekrarlayan
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.recurringCard.visibility = View.VISIBLE
                binding.onceCard.visibility = View.GONE
            } else {
                binding.recurringCard.visibility = View.GONE
                binding.onceCard.visibility = View.VISIBLE
            }
        }

        // Date Picker
        binding.btnDate.setOnClickListener {
            showDatePicker()
        }

        // Time Picker
        binding.btnTime.setOnClickListener {
            showTimePicker()
        }

        // Gün başına zaman butonları
        setupDayTimeButton(R.id.btn_pazartesi_time, 1)
        setupDayTimeButton(R.id.btn_sali_time, 2)
        setupDayTimeButton(R.id.btn_carsamba_time, 3)
        setupDayTimeButton(R.id.btn_persembe_time, 4)
        setupDayTimeButton(R.id.btn_cuma_time, 5)
        setupDayTimeButton(R.id.btn_cumartesi_time, 6)
        setupDayTimeButton(R.id.btn_pazar_time, 7)

        // Kaydet
        binding.btnSave.setOnClickListener {
            saveTimeTrigger()
        }
    }

    private fun setupDayTimeButton(buttonId: Int, dayOfWeek: Int) {
        findViewById<com.google.android.material.button.MaterialButton>(buttonId).setOnClickListener {
            val currentTime = scheduleMap[dayOfWeek]
            TimePickerDialog(this, { _, hour, minute ->
                scheduleMap[dayOfWeek] = TimeSchedule(dayOfWeek, hour, minute)
                val button = findViewById<com.google.android.material.button.MaterialButton>(buttonId)
                button.text = String.format("%02d:%02d", hour, minute)
            }, currentTime?.hour ?: 18, currentTime?.minute ?: 0, true).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedYear = year
                selectedMonth = month + 1
                selectedDay = day
                updateSelectedDateTimeText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(this, { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
            updateSelectedDateTimeText()
        }, selectedHour, selectedMinute, true).show()
    }

    private fun updateSelectedDateTimeText() {
        binding.tvSelectedDatetime.text = "Seçili: $selectedDay.$selectedMonth.$selectedYear $selectedHour:$selectedMinute"
    }

    private fun loadExistingTrigger() {
        if (triggerId > 0) {
            lifecycleScope.launch {
                val trigger = database.triggerDao().getTriggerById(triggerId)
                trigger?.let {
                    val params = TimeParams.fromJson(it.params)
                    if (params.type == "RECURRING" && params.schedule != null) {
                        // Mevcut schedule'ı yükle
                        params.schedule.forEach { schedule ->
                            scheduleMap[schedule.day] = schedule
                            updateDayButtonText(schedule.day, schedule.hour, schedule.minute)
                        }
                    } else if (params.type == "ONCE") {
                        // Tek seferlik ise değerleri yükle
                        selectedYear = params.year ?: 0
                        selectedMonth = params.month ?: 0
                        selectedDay = params.day ?: 0
                        selectedHour = params.hour ?: 18
                        selectedMinute = params.minute ?: 0
                        updateSelectedDateTimeText()
                        // Switch'i OFF yap (tek seferlik)
                        binding.switchRecurring.isChecked = false
                        binding.recurringCard.visibility = View.GONE
                        binding.onceCard.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateDayButtonText(dayOfWeek: Int, hour: Int, minute: Int) {
        val buttonId = when (dayOfWeek) {
            1 -> R.id.btn_pazartesi_time
            2 -> R.id.btn_sali_time
            3 -> R.id.btn_carsamba_time
            4 -> R.id.btn_persembe_time
            5 -> R.id.btn_cuma_time
            6 -> R.id.btn_cumartesi_time
            7 -> R.id.btn_pazar_time
            else -> return
        }
        findViewById<com.google.android.material.button.MaterialButton>(buttonId).text = 
            String.format("%02d:%02d", hour, minute)
    }

    private fun saveTimeTrigger() {
        Log.d("TimeSetupActivity", "🟢 KAYDET BUTONUNA BASILDI - scenarioId: $scenarioId, triggerId: $triggerId")

        // Android 12+ exact alarm izni kontrolü
        if (!timeTriggerManager.canScheduleExactAlarms()) {
            Log.w("TimeSetupActivity", "⚠️ Exact alarm izni yok! Settings'e yönlendiriliyor...")
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(this, "Exact alarm izni gerekli! İzin verdikten sonra tekrar deneyin.", Toast.LENGTH_LONG).show()
                return
            } catch (e: Exception) {
                Log.e("TimeSetupActivity", "Settings yönlendirme hatası: ${e.message}")
                Toast.makeText(this, "Lütfen Settings > Apps > Vetroid > Alarm izinlerinden exact alarm iznini aktif edin.", Toast.LENGTH_LONG).show()
                return
            }
        }

        val isRecurring = binding.switchRecurring.isChecked

        lifecycleScope.launch {
            try {
                val params: TimeParams

                if (isRecurring) {
                    // Tekrarlayan - seçili günleri topla
                    val selectedDays = mutableListOf<TimeSchedule>()

                    if (binding.cbPazartesi.isChecked) selectedDays.add(scheduleMap[1]!!)
                    if (binding.cbSali.isChecked) selectedDays.add(scheduleMap[2]!!)
                    if (binding.cbCarsamba.isChecked) selectedDays.add(scheduleMap[3]!!)
                    if (binding.cbPersembe.isChecked) selectedDays.add(scheduleMap[4]!!)
                    if (binding.cbCuma.isChecked) selectedDays.add(scheduleMap[5]!!)
                    if (binding.cbCumartesi.isChecked) selectedDays.add(scheduleMap[6]!!)
                    if (binding.cbPazar.isChecked) selectedDays.add(scheduleMap[7]!!)

                    Log.d("TimeSetupActivity", "📅 Seçili günler: ${selectedDays.map { it.day }}, toplam: ${selectedDays.size}")

                    if (selectedDays.isEmpty()) {
                        Toast.makeText(this@TimeSetupActivity, "En az bir gün seçin", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    params = TimeParams(type = "RECURRING", schedule = selectedDays)
                    Log.d("TimeSetupActivity", "📋 Params oluşturuldu: ${params.toJson().substring(0, Math.min(100, params.toJson().length))}")


                } else {
                    // Tek seferlik
                    if (selectedDay == 0) {
                        Toast.makeText(this@TimeSetupActivity, "Tarih seçin", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    params = TimeParams(
                        type = "ONCE",
                        year = selectedYear,
                        month = selectedMonth,
                        day = selectedDay,
                        hour = selectedHour,
                        minute = selectedMinute
                    )

                    Log.d("TimeSetupActivity", "⏰ Tek seferlik: $selectedDay.$selectedMonth.$selectedYear $selectedHour:$selectedMinute")
                }

                // Trigger'ı kaydet veya güncelle
                if (triggerId > 0) {
                    Log.d("TimeSetupActivity", "🔄 UPDATE MODUNDA - Mevcut triggerId: $triggerId")
                    val existingTrigger = database.triggerDao().getTriggerById(triggerId)
                    if (existingTrigger != null) {
                        val updated = existingTrigger.copy(params = params.toJson(), isActive = true)
                        database.triggerDao().update(updated)
                        Log.d("TimeSetupActivity", "✅ Trigger GÜNCELLENDI: $triggerId")
                    } else {
                        Log.w("TimeSetupActivity", "⚠️ Trigger ID: $triggerId bulunamadı! Yeni oluşturulacak")
                        triggerId = 0
                    }
                }
                
                if (triggerId == 0L) {
                    Log.d("TimeSetupActivity", "➕ INSERT MODUNDA - Yeni Trigger oluşturulacak")
                    Log.d("TimeSetupActivity", "   - scenarioId: $scenarioId")
                    Log.d("TimeSetupActivity", "   - type: TIME")
                    
                    val trigger = Trigger(
                        scenarioId = scenarioId,
                        type = "TIME",
                        params = params.toJson(),
                        isActive = true
                    )
                    
                    triggerId = database.triggerDao().insert(trigger)
                    Log.d("TimeSetupActivity", "✅ YENI TRIGGER OLUŞTURULDU: $triggerId")
                }

                // ✅ SONRA Alarmları kur (triggerId artık kesinlikle > 0)
                if (isRecurring && triggerId > 0) {
                    val paramsAfterInsert = TimeParams.fromJson(params.toJson())
                    paramsAfterInsert.schedule?.forEach { schedule ->
                        Log.d("TimeSetupActivity", "⏱️ Alarm kuruluyor - Gün: ${schedule.day}, Saat: ${schedule.hour}:${schedule.minute}, TriggerId: $triggerId")
                        timeTriggerManager.scheduleAlarm(triggerId, schedule.day, schedule.hour, schedule.minute)
                    }
                }
                
                // Artık SharedPreferences'a gerek yok, database'den alıyoruz
                Log.d("TimeSetupActivity", "💾 Trigger veritabanında kayıtlı: $triggerId")

                runOnUiThread {
                    Toast.makeText(this@TimeSetupActivity, "✅ Tarih/Saat tetikleyicisi kaydedildi!", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                Log.e("TimeSetupActivity", "❌ EXCEPTION: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@TimeSetupActivity, "❌ Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}