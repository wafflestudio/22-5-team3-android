package com.example.wafflestudio_toyproject.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.wafflestudio_toyproject.AppUtils
import com.example.wafflestudio_toyproject.AuthApi
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


        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenAuthenticator", "No Refresh Token Found")
            return null
        }

        Log.d("TokenAuthenticator", "Attempting to refresh access token with refresh token: $refreshToken")

        val newAccessToken = refreshAccessToken(refreshToken)
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

    private fun navigateToLogin() {
        // 토큰 삭제
        sharedPreferences.edit().clear().apply()
        AppUtils.navigateToLoginScreen(context)
    }
}
