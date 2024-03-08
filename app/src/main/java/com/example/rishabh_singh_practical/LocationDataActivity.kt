package com.example.rishabh_singh_practical

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rishabh_singh_practical.databinding.ActivityLocationDataBinding
import java.io.File
import java.util.Scanner

class LocationDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationDataBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read and display the location data from the CSV file
        val locationData = readLocationDataFromCSV()
        binding.tvLocationDataContent.text = locationData.joinToString("\n") { "${it.first}: ${it.second.first}, ${it.second.second}" }

        binding.btnClose.setOnClickListener {
            finish() // Close the activity when the Close button is clicked
        }
    }

    private fun readLocationDataFromCSV(): List<Pair<String, Pair<Double, Double>>> {
        val locationData = mutableListOf<Pair<String, Pair<Double, Double>>>()
        try {
            val file = File(getExternalFilesDir(null), "location_data.csv")
            val fileReader = Scanner(file)
            while (fileReader.hasNextLine()) {
                val line = fileReader.nextLine()
                val parts = line.split(",")
                if (parts.size == 3) {
                    val timestamp = parts[0]
                    val latitude = parts[1].toDouble()
                    val longitude = parts[2].toDouble()
                    locationData.add(timestamp to (latitude to longitude))
                }
            }
            fileReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locationData
    }
}