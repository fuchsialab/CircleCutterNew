package com.fuchsialab.circlecutter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.ads.FullScreenContentCallback as FullScreenContentCallback1

class ResultImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    var mAuth: FirebaseAuth? = null
    var mDatabase: DatabaseReference? = null

    private var bannerid: String? = null
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_image)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

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

        if (mInterstitialAd != null) {
            mInterstitialAd!!.show(this@ResultImageActivity)

            mInterstitialAd!!.setFullScreenContentCallback(object :
                FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    val adRequest = AdRequest.Builder().build()

                    interstitialId?.let {
                        InterstitialAd.load(
                            this@ResultImageActivity,
                            it,
                            adRequest,
                            object : InterstitialAdLoadCallback() {
                                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                    mInterstitialAd = interstitialAd
                                }

                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                    mInterstitialAd = null
                                }
                            })
                    }
                }
            })
        }

    }

    private fun saveImage() {

        val drawable = imageView.drawable as? BitmapDrawable
        val bitmap = drawable?.bitmap

        if (bitmap != null) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "saved_image_$timeStamp.png"

            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (storageDir != null && (storageDir.exists() || storageDir.mkdirs())) {
                val outputFile = File(storageDir, filename)
                FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", outputFile
                )

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
        } else {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
        }
    }

    fun bannerAds() {
        val rootref = FirebaseDatabase.getInstance().reference.child("AdUnits")
        rootref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("MissingPermission")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bannerid =
                    Objects.requireNonNull(dataSnapshot.child("banner").value).toString()
                interstitialId =
                    Objects.requireNonNull(dataSnapshot.child("Interstitial").value).toString()

                val view = findViewById<View>(R.id.adView)
                mAdView = AdView(this@ResultImageActivity)
                (view as RelativeLayout).addView(mAdView)
                mAdView!!.setAdSize(AdSize.BANNER)
                mAdView!!.adUnitId = bannerid as String
                val adRequest = AdRequest.Builder().build()
                mAdView!!.loadAd(adRequest)

                //MediationTestSuite.launch(MainActivity.this);
                InterstitialAd.load(
                    this@ResultImageActivity,
                    interstitialId!!,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            mInterstitialAd = null
                        }
                    })
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}
