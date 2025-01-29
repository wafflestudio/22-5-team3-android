package com.example.wafflestudio_toyproject

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("App", "KAKAO_NATIVE_KEY: '${BuildConfig.KAKAO_NATIVE_KEY}'")

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)

        var keyHash = Utility.getKeyHash(this)
        Log.d("App", "keyHash: '${keyHash}'")

        NaverIdLoginSDK.initialize(
            this,
            "xwnPExvfFVbdUANM_CW5",
            BuildConfig.NAVER_CLIENT_SECRET,
            "SNUVote"
        )
    }

}
