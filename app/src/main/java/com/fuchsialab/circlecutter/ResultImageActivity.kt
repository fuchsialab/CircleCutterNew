package com.fuchsialab.circlecutter

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ResultImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    private var bannerid: String? = null
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)

        bannerAds()

        MobileAds.initialize(
            this
        ) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    "MyApp", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status!!.description, status.latency
                    )
                )
            }
            // Start loading ads here...
        }


        imageView = findViewById(R.id.resultImageView)
        val imageUriString = intent.getStringExtra("imageUri")
        imageUri = Uri.parse(imageUriString)

        imageView.setImageURI(imageUri)

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            showAds()
            saveImage()

        }
    }

    private fun showAds(){
        
        if (Admob.mInterstitialAd != null) {
            Admob.mInterstitialAd!!.show(this)
            Admob.mInterstitialAd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Admob.mInterstitialAd = null
                        Admob.loadInter(this@ResultImageActivity)

                    }
                }
        } else {
            Admob.loadInter(this@ResultImageActivity)

        }

    }

    private fun saveImage() {
        val drawable = imageView.drawable as? BitmapDrawable
        val bitmap = drawable?.bitmap

        if (bitmap != null) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "circle_cutter_$timeStamp.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10 and higher
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    try {
                        val outputStream = resolver.openOutputStream(uri)
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()

                            runOnUiThread {
                                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Use traditional method for Android 9 and lower
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (storageDir != null && (storageDir.exists() || storageDir.mkdirs())) {
                    val outputFile = File(storageDir, filename)
                    try {
                        val outputStream = FileOutputStream(outputFile)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        // Notify the media scanner about the new file
                        MediaScannerConnection.scanFile(this, arrayOf(outputFile.absolutePath), null) { path, uri ->
                            runOnUiThread {
                                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to access storage", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
        }
    }


//    private fun saveImage() {
//
//        val drawable = imageView.drawable as? BitmapDrawable
//        val bitmap = drawable?.bitmap
//
//        if (bitmap != null) {
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val filename = "saved_image_$timeStamp.png"
//
//            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//            if (storageDir != null && (storageDir.exists() || storageDir.mkdirs())) {
//                val outputFile = File(storageDir, filename)
//                FileProvider.getUriForFile(
//                    this, "${packageName}.fileprovider", outputFile
//                )
//
//                try {
//                    val outputStream = FileOutputStream(outputFile)
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                    outputStream.flush()
//                    outputStream.close()
//
//                    // Notify the media scanner about the new file
//                    MediaScannerConnection.scanFile(this, arrayOf(outputFile.absolutePath), null) { path, uri ->
//                        runOnUiThread {
//                            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "Failed to access storage", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
//        }
//    }

    fun bannerAds() {

        bannerid = resources.getString(R.string.bannerid)
        val view = findViewById<View>(R.id.adView)
        mAdView = AdView(this@ResultImageActivity)
        (view as RelativeLayout).addView(mAdView)
        mAdView!!.setAdSize(AdSize.BANNER)
        mAdView!!.adUnitId = bannerid as String
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

    }
}
