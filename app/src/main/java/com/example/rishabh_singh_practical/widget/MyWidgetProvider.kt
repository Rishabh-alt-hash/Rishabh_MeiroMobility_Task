package com.example.rishabh_singh_practical.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.example.rishabh_singh_practical.CameraActivity
import com.example.rishabh_singh_practical.LocationService
import com.example.rishabh_singh_practical.R

class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Handle button clicks
        if (intent.action == "CLICK_IMAGE") {
            val appIntent = Intent(context, CameraActivity::class.java)
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(appIntent)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            Log.e("WidgetProvider", "Widget layout")

            // Click Image button
            val clickImageIntent = Intent(context, WidgetReceiver::class.java)
            clickImageIntent.action = "CLICK_IMAGE"
            val clickImagePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickImageIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnClickImage, clickImagePendingIntent)
            Toast.makeText(context, "Camera started", Toast.LENGTH_SHORT).show()

            // Location Tracking switch
            val switchIntent = Intent(context, LocationService::class.java)
            val switchPendingIntent = PendingIntent.getService(
                context,
                0,
                switchIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.switchLocationService, switchPendingIntent)
            Toast.makeText(context, "Service started", Toast.LENGTH_SHORT).show()

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}