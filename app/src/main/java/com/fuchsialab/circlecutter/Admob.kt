package com.fuchsialab.circlecutter

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object Admob {
    var mInterstitialAd: InterstitialAd? = null
    fun loadInter(context: Context) {
        MobileAds.initialize(context) { }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.interstitialId), adRequest,
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