package com.example.vetroid.executor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.util.Log
import com.example.vetroid.data.Constraint
import org.json.JSONObject
import java.util.Calendar

class ConstraintChecker(private val context: Context) {

    companion object {
        const val TYPE_TIME_WINDOW = "TIME_WINDOW"
        const val TYPE_WIFI       = "WIFI"
        const val TYPE_CHARGING   = "CHARGING"
        private const val TAG = "ConstraintChecker"
    }

    // Listedeki tüm aktif kısıtlamaların sağlanıp sağlanmadığını döner.
    // Biri bile karşılanmıyorsa false döner.
    fun checkAll(constraints: List<Constraint>): Boolean {
        for (constraint in constraints) {
            if (!constraint.isActive) continue
            val satisfied = check(constraint)
            if (!satisfied) {
                Log.d(TAG, "Kısıtlama karşılanmadı: ${constraint.type} — senaryo atlandı")
                return false
            }
        }
        return true
    }

    private fun check(constraint: Constraint): Boolean {
        return try {
            when (constraint.type) {
                TYPE_TIME_WINDOW -> checkTimeWindow(JSONObject(constraint.params))
                TYPE_WIFI        -> checkWifi(JSONObject(constraint.params))
                TYPE_CHARGING    -> checkCharging(JSONObject(constraint.params))
                else             -> true // Bilinmeyen tür geçer
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kısıtlama kontrolü hatası (${constraint.type}): ${e.message}")
            true // Hata durumunda engelleme yapma
        }
    }

    // params: {"startHour": 8, "startMinute": 0, "endHour": 20, "endMinute": 0}
    private fun checkTimeWindow(params: JSONObject): Boolean {
        val startHour   = params.getInt("startHour")
        val startMinute = params.getInt("startMinute")
        val endHour     = params.getInt("endHour")
        val endMinute   = params.getInt("endMinute")

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes   = startHour * 60 + startMinute
        val endMinutes     = endHour   * 60 + endMinute

        return if (startMinutes <= endMinutes) {
            // Normal aralık: örn 08:00 - 20:00
            currentMinutes in startMinutes..endMinutes
        } else {
            // Gece yarısını geçen aralık: örn 22:00 - 06:00
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    // params: {"requireConnected": true}
    private fun checkWifi(params: JSONObject): Boolean {
        val requireConnected = params.optBoolean("requireConnected", true)
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return !requireConnected
        val caps    = cm.getNetworkCapabilities(network) ?: return !requireConnected
        val isWifiConnected = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        return if (requireConnected) isWifiConnected else !isWifiConnected
    }

    // params: {"requireCharging": true}
    private fun checkCharging(params: JSONObject): Boolean {
        val requireCharging = params.optBoolean("requireCharging", true)
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL
        return if (requireCharging) isCharging else !isCharging
    }
}
