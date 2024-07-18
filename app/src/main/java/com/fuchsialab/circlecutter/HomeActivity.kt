package com.fuchsialab.circlecutter

import android.annotation.SuppressLint
import android.app.ProgressDialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.fuchsialab.circlecutter.Admob.loadInter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hotchemi.android.rate.AppRate
import java.util.Objects
import java.util.Timer
import java.util.TimerTask


class HomeActivity : AppCompatActivity() {
    var navigationView: NavigationView? = null
    var toggle: ActionBarDrawerToggle? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var toolba2r: Toolbar? = null

    private var backPressTime: Long = 0

    var mAuth: FirebaseAuth? = null
    var mDatabase: DatabaseReference? = null

    private var bannerid: String? = null
    private var update: String? = null
    private var mAdView: AdView? = null
    private var appVersion: String? = null

    var button: Button? = null
    var progressDialog: ProgressDialog? = null
    var timer: Timer? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        if (firstStart) {
            showStartDialog()
        }

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

        appVersion = getAppVersion(this)


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.root_layout)
        navigationView = findViewById(R.id.navdrawer)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close)
        drawerLayout?.addDrawerListener(toggle!!)
        toggle!!.syncState()
        toggle!!.drawerArrowDrawable.color = resources.getColor(R.color.abba)


        progressDialog = ProgressDialog(this@HomeActivity)
        progressDialog!!.show()
        progressDialog?.setContentView(R.layout.progress)
        Objects.requireNonNull(progressDialog!!.window)
            ?.setBackgroundDrawableResource(android.R.color.transparent)

        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                progressDialog!!.dismiss()
            }
        }, 2000)


        AppRate.with(this)
            .setInstallDays(0)
            .setLaunchTimes(5)
            .setRemindInterval(10)
            .setShowLaterButton(true)
            .setDebug(false)
            .setOnClickButtonListener { which ->
                Log.d(
                    MainActivity::class.java.name,
                    which.toString()
                )
            }
            .monitor()

        AppRate.showRateDialogIfMeetsConditions(this)

        button = findViewById(R.id.rx_sample_button)

        button?.setOnClickListener(View.OnClickListener {

            if (Admob.mInterstitialAd != null) {
                Admob.mInterstitialAd!!.show(this)
                Admob.mInterstitialAd!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Admob.mInterstitialAd = null
                            loadInter(this@HomeActivity)

                            val intent = Intent(
                                this@HomeActivity,
                                MainActivity::class.java
                            )
                            startActivity(intent)
                        }
                    }
            } else {
                loadInter(this@HomeActivity)

                val intent = Intent(
                    this@HomeActivity,
                    MainActivity::class.java
                )
                startActivity(intent)

            }

        })


        navigationView?.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuHome -> {
                    drawerLayout?.closeDrawer(GravityCompat.START)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.menuprivacy -> {
                    val browse = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.lkmkm)))
                    startActivity(browse)
                    drawerLayout?.closeDrawer(GravityCompat.START)

                    return@OnNavigationItemSelectedListener true
                }

                R.id.menurate -> {
                    drawerLayout?.closeDrawer(GravityCompat.START)
                    val appPackageName = packageName
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    "market://details?id=$appPackageName"
                                )
                            )
                        )
                    } catch (anfe: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id=$appPackageName"
                                )
                            )
                        )
                    }

                    return@OnNavigationItemSelectedListener true
                }


                R.id.menumoreapp -> {
                    val browses = Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            ("https://play.google.com/store/apps/collection/cluster?clp=igM4ChkKEzUzNjIwODY3OTExNjgyNTA2MTkQCBgDEhkKEzUzNjIwODY3OTExNjgyNTA2MTkQCBgDGAA%3D:S:ANO1ljJMw2s&gsr=CjuKAzgKGQoTNTM2MjA4Njc5MTE2ODI1MDYxORAIGAMSGQoTNTM2MjA4Njc5MTE2ODI1MDYxORAIGAMYAA%3D%3D:S:ANO1ljI3U6g")
                        )
                    )
                    startActivity(browses)
                    drawerLayout?.closeDrawer(GravityCompat.START)

                    return@OnNavigationItemSelectedListener true
                }

                R.id.menushare -> {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.setType("text/plain")
                    val shareBody =
                        "Download Circle Cutter App and Crop your picture in circle shape .  https://play.google.com/store/apps/details?id=com.fuchsialab.circlecutter"
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle Cutter")
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                    startActivity(Intent.createChooser(sharingIntent, "Share via"))
                    drawerLayout?.closeDrawers()

                    return@OnNavigationItemSelectedListener true
                }

                R.id.menuexit -> {
                    finishAffinity()

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun onUpdateCheck() {
        val appPackageName = packageName

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New Version Available")
            .setMessage("Please update for better experience")
            .setPositiveButton("UPDATE") { dialogInterface, i ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/apps/details?id=$appPackageName"
                        )
                    )
                )
            }
            .setNegativeButton("NOT NOW") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        alertDialog.show()
    }


    override fun onBackPressed() {
        if (backPressTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }

        backPressTime = System.currentTimeMillis()
    }


    private fun showStartDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("This app need your internet permission, storage permission. Data permission is needed for showing advertisement. We always aware of your privacy. Please tap on 'Ok' if you want to continue.")
            .setPositiveButton(
                "Ok"
            ) { dialog, which -> dialog.dismiss() }
            .create().show()

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false)
        editor.apply()
    }


    fun bannerAds() {
        val rootref = FirebaseDatabase.getInstance().reference.child("AdUnits")
        rootref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("MissingPermission")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                bannerid = resources.getString(R.string.bannerid)

                update =
                    Objects.requireNonNull(dataSnapshot.child("Update").value).toString()

                if (update!=null){
                    if (appVersion!=update){
                        onUpdateCheck()
                    }
                }

                val view = findViewById<View>(R.id.adView)
                mAdView = AdView(this@HomeActivity)
                (view as RelativeLayout).addView(mAdView)
                mAdView!!.adUnitId = bannerid as String
                mAdView!!.setAdSize(AdSize.BANNER)
                val adRequest = AdRequest.Builder().build()
                mAdView!!.loadAd(adRequest)

                //MediationTestSuite.launch(MainActivity.this);
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }


    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            result = context.packageManager.getPackageInfo(context.packageName, 0)
                .versionName
            result = result.replace("[a-zA-Z]|-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return result
    }
}


