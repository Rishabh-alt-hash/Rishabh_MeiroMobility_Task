package com.example.rishabh_singh_practical

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private var notificationManager: NotificationManagerCompat? = null
    private var notificationBuilder: NotificationCompat.Builder? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle location updates here
            val latitude = location.latitude
            val longitude = location.longitude
            Log.e(TAG, "$latitude, $longitude")

            // Display location in notification bar
            startForegroundNotification(latitude, longitude)

            //Store location data in CSV file
            storeLocationData(latitude, longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.e(TAG, "$locationManager")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       /* if (intent?.action == "START_SERVICE") {
            startLocationUpdates()
        } else if (intent?.action == "STOP_SERVICE") {
            stopLocationService()
        }*/
        if (checkLocationPermission() && checkNotificationPermission()) {
            startLocationUpdates()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startLocationUpdates() {
        try {
            Log.e(TAG, "Requesting location updates")
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            Log.e(TAG, "Network Provider Enabled: $isNetworkEnabled, GPS Provider Enabled: $isGpsEnabled")

            if (!isNetworkEnabled || !isGpsEnabled) {
                Log.e(TAG, "Location providers are disabled. Enabling location services...")
                enableLocationServices()
            } else {
                Log.e(TAG, "Location providers are enabled.")
            }
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_INTERVAL,
                LOCATION_DISTANCE,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL,
                LOCATION_DISTANCE,
                locationListener
            )
        } catch (ex: SecurityException) {
            Log.e(TAG, "Location permissions error: ${ex.message}")
        }
    }

    private fun stopLocationService() {
        locationManager.removeUpdates(locationListener)
        stopForeground(true)
        stopSelf()
    }

    private fun enableLocationServices(){
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(settingsIntent)
    }

    private fun startForegroundNotification(latitude: Double, longitude: Double) {
        val locationText = "$latitude, $longitude"
        Log.e("LocationText", locationText)

        if (notificationManager == null || notificationBuilder == null) {
            notificationManager = NotificationManagerCompat.from(this)
            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }

        // Update notification content
        notificationBuilder?.setContentText(locationText)

        val notificationIntent = Intent(this, LocationDataActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder?.setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(FOREGROUND_SERVICE_ID, notificationBuilder?.build())
    }

    private fun storeLocationData(latitude: Double, longitude: Double) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val file = File(getExternalFilesDir(null), "location_data.csv")
            val fileWriter = FileWriter(file, true)

            fileWriter.append("$currentDate,$latitude,$longitude\n")
            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FULL_SCREEN_INTENT
                ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    companion object {
        private val TAG = LocationService::class.java.toString()
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val FOREGROUND_SERVICE_ID = 12345
        private const val LOCATION_INTERVAL: Long = 2000 // Location update interval in milliseconds
        private const val LOCATION_DISTANCE: Float = 0f // Location update distance in meters
    }
}