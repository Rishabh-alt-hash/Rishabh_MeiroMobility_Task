package com.example.rishabh_singh_practical.widget

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.rishabh_singh_practical.CameraActivity
import com.example.rishabh_singh_practical.LocationService
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "CLICK_IMAGE_ACTION" -> {
                // Open CameraActivity
                val cameraIntent = Intent(context, CameraActivity::class.java)
                cameraIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(cameraIntent)
            }
            "TOGGLE_SERVICE" -> {
                // Start or stop LocationService based on switch state
                val serviceIntent = Intent(context, LocationService::class.java)
                if (isServiceRunning(context, LocationService::class.java)) {
                    // Stop the service
                    context.stopService(serviceIntent)
                } else {
                    // Start the service
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            }
        }
    }


    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
