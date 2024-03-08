package com.example.rishabh_singh_practical

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import com.example.rishabh_singh_practical.databinding.ActivityCameraBinding
import java.io.File
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    private lateinit var cameraExecutor: ExecutorService

    private val cameraOutputDirectory: File by lazy {
        getOutputDirectory()
    }

    val imageCapture = ImageCapture.Builder()
        .setTargetRotation(Surface.ROTATION_0)
        .build()

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
    val photoFile = File(
        cameraOutputDirectory,
        SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    val bitmap = binding.textureView.bitmap
    val textureViewWidth = binding.textureView.width
    val textureViewHeight = binding.textureView.height

    // Calculate the coordinates of the middle rectangle in the TextureView
    val middleRectangle = binding.middleRectangle
    val middleRectLeft = (textureViewWidth - middleRectangle.width) / 2
    val middleRectTop = (textureViewHeight - middleRectangle.height) / 2

    // Crop the bitmap to the middle rectangle area
    val croppedBitmap = Bitmap.createBitmap(
        bitmap!!,
        middleRectLeft,
        middleRectTop,
        middleRectangle.width,
        middleRectangle.height
    )

    // Save the cropped bitmap to a file
    try {
        FileOutputStream(photoFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Notify the user that the photo capture was successful
        val msg = "Photo capture succeeded: ${Uri.fromFile(photoFile)}"
        Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_LONG).show()

        // Pass the file path to the next activity
        val intent = Intent(this@CameraActivity, ImagePreviewActivity::class.java)
        intent.putExtra("imageFilePath", photoFile.absolutePath)
        startActivity(intent)
    } catch (e: IOException) {
        // Handle errors saving the photo
        e.printStackTrace()
        Toast.makeText(this@CameraActivity, "Failed to save photo", Toast.LENGTH_SHORT).show()
    }
}


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(object : Preview.SurfaceProvider {
                        override fun onSurfaceRequested(request: SurfaceRequest) {
                            val surface = Surface(binding.textureView.surfaceTexture)
                            request.provideSurface(surface, cameraExecutor, { surface.release() })
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Toast.makeText(this, "Error starting the camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this@CameraActivity,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}