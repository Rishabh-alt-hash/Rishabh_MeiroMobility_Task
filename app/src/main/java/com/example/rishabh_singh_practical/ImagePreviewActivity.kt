package com.example.rishabh_singh_practical

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rishabh_singh_practical.databinding.ActivityImagePreviewBinding

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageFilePath = intent.getStringExtra("imageFilePath")
        val bitmap = BitmapFactory.decodeFile(imageFilePath)
        bitmap?.let {
            binding.image.setImageBitmap(bitmap)
        }

    }
}