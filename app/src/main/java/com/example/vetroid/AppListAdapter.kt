package com.example.vetroid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val apps: List<ApplicationInfo>,
    private val onAppClicked: (ApplicationInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_app_icon)
        val name: TextView = view.findViewById(R.id.tv_app_name)
        val packageName: TextView = view.findViewById(R.id.tv_package_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = apps[position]
        val context = holder.itemView.context

        // App icon
        try {
            holder.icon.setImageDrawable(appInfo.loadIcon(context.packageManager))
        } catch (e: Exception) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // App name
        holder.name.text = context.packageManager.getApplicationLabel(appInfo)

        // Package name
        holder.packageName.text = appInfo.packageName

        // Click listener
        holder.itemView.setOnClickListener {
            onAppClicked(appInfo)
        }
    }

    override fun getItemCount(): Int = apps.size
}