package com.example.wafflestudio_toyproject.di

import android.content.SharedPreferences
import com.example.wafflestudio_toyproject.AuthApi
import com.example.wafflestudio_toyproject.RefreshTokenRequest
import com.example.wafflestudio_toyproject.UserApi
import com.example.wafflestudio_toyproject.UserRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @Named("AuthApi") private val authApi: AuthApi // AuthAPI를 주입받음
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
            ?: throw IllegalStateException("No Refresh Token Found")

        val newAccessToken = refreshAccessToken(refreshToken)
        return if (!newAccessToken.isNullOrEmpty()) {
            response.request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val refreshRequest = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(refreshRequest).execute()

            if (response.isSuccessful) {
                val newAccessToken = response.body()?.access_token
                if (!newAccessToken.isNullOrEmpty()) {
                    sharedPreferences.edit().apply {
                        putString("access_token", newAccessToken)
                        apply()
                    }
                    newAccessToken
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}






