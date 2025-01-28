package com.example.wafflestudio_toyproject.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.wafflestudio_toyproject.AppUtils
import com.kakao.sdk.auth.TokenManager
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @Named("AuthApi") private val authApi: AuthApi, // AuthAPI를 주입받음
    @ApplicationContext private val context: Context
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        val kakaoAccessToken = getKakaoAccessToken()
        val naverAccessToken = getNaverAccessToken()

        Log.d("TokenAuthenticator", "Attempting to refresh access token")

        val newAccessToken = when {
            !refreshToken.isNullOrEmpty() -> refreshAccessToken(refreshToken) // 일반 로그인 사용자의 경우
            !kakaoAccessToken.isNullOrEmpty() -> kakaoAccessToken // 카카오 로그인 사용자의 경우
            !naverAccessToken.isNullOrEmpty() -> naverAccessToken // 네이버 로그인 사용자의 경우
            else -> null
        }

        return if (!newAccessToken.isNullOrEmpty()) {
            Log.d("TokenAuthenticator", "New access token acquired: $newAccessToken")
            response.request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            Log.e("TokenAuthenticator", "Failed to refresh access token")
            navigateToLogin()
            null
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val authorizationHeader = "Bearer $refreshToken"
            val response = authApi.refreshToken(authorizationHeader).execute()

            if (response.isSuccessful) {
                response.body()?.access_token?.also { newAccessToken ->
                    sharedPreferences.edit().putString("access_token", newAccessToken).apply()
                }
            } else {
                Log.e("TokenAuthenticator", "Refresh token failed: ${response.code()} ${response.message()}")
                navigateToLogin()
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Exception during token refresh", e)
            navigateToLogin()
            null
        }
    }

    private fun getKakaoAccessToken(): String? {
        return try {
            TokenManager.instance.getToken()?.accessToken
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Failed to get Kakao access token", e)
            null
        }
    }

    private fun getNaverAccessToken(): String? {
        return try {
            NaverIdLoginSDK.getAccessToken()
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Failed to get Naver access token", e)
            null
        }
    }

    fun applyNewAccessToken(newAccessToken: String) {
        Log.d("TokenAuthenticator", "새로운 access_token 즉시 반영: $newAccessToken")

        sharedPreferences.edit()
            .putString("access_token", newAccessToken)
            .apply()
    }

    private fun navigateToLogin() {
        // 토큰 삭제
        sharedPreferences.edit().clear().apply()
        AppUtils.navigateToLoginScreen(context)
    }
}
