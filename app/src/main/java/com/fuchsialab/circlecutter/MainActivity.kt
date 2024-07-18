package com.fuchsialab.circlecutter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.isseiaoki.simplecropview.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private val REQUEST_CODE_LOAD_IMAGE = 100
    private val REQUEST_CODE_STORAGE_PERMISSION = 101
    private var backPressedTime: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cropImageView = findViewById(R.id.cropImageView)
        cropImageView.setCropMode(CropImageView.CropMode.CIRCLE)

        val loadImageButton = findViewById<Button>(R.id.loadImageButton)
        loadImageButton.setOnClickListener {
            if (checkPermissions()) {
                openGallery()
            } else {
                requestPermissions()
            }
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (cropImageView.isLaidOut) {
                val croppedBitmap = cropImageView.getCroppedBitmap()
                if (croppedBitmap != null) {
                    saveCroppedBitmap(croppedBitmap)
                } else {
                    Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "CropImageView is not ready", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_LOAD_IMAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            Log.d("MainActivity", "Image URI: $imageUri")
            imageUri?.let {
                cropImageView.load(it).execute(object : com.isseiaoki.simplecropview.callback.LoadCallback {
                    override fun onError(e: Throwable?) {
                        Toast.makeText(this@MainActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                        Log.e("MainActivity", "Load error", e)
                    }

                    override fun onSuccess() {
                        Toast.makeText(this@MainActivity, "Image loaded successfully", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun saveCroppedBitmap(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "cropped_image_$timeStamp.png"

        // Save the image temporarily in the cache directory
        val cacheDir = cacheDir
        val imageFile = File(cacheDir, filename)

        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Get the URI for the image file
            val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)

            // Open ResultImageActivity with the image URI
            val resultIntent = Intent(this, ResultImageActivity::class.java).apply {
                putExtra("imageUri", imageUri.toString())
            }
            startActivity(resultIntent)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Press back again to go back", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
