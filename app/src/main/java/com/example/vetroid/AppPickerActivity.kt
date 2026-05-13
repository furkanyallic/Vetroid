package com.example.vetroid

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppPickerActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_apps)

        val apps = getInstalledApps()
        Log.d("AppPickerActivity", "Launchable app count: ${apps.size}")

        adapter = AppListAdapter(apps) { appInfo ->
            val resultIntent = Intent().apply {
                putExtra(EXTRA_PACKAGE_NAME, appInfo.packageName)
                putExtra(EXTRA_APP_NAME, getAppName(appInfo))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun getInstalledApps(): List<ApplicationInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        @Suppress("DEPRECATION")
        val launchableActivities = packageManager.queryIntentActivities(launcherIntent, 0)

        return launchableActivities
            .mapNotNull { it.activityInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { getAppName(it).lowercase() }
    }

    private fun getAppName(appInfo: ApplicationInfo): String {
        return packageManager.getApplicationLabel(appInfo).toString()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_APP_NAME = "app_name"
    }
}
