package com.example.wafflestudio_toyproject.network

import android.content.Context
import android.content.SharedPreferences
import com.example.wafflestudio_toyproject.AuthApi
import com.example.wafflestudio_toyproject.UserApi
import com.example.wafflestudio_toyproject.VoteApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://52.78.27.95/"

    // 일반 API용 OkHttpClient (Authenticator 포함)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        sharedPreferences: SharedPreferences,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(tokenAuthenticator) // Authenticator 추가
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val accessToken = sharedPreferences.getString("access_token", null)

                if (!accessToken.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $accessToken")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // AuthAPI 전용 OkHttpClient (Authenticator 미포함)
    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // 일반 API용 Retrofit (OkHttpClient 사용)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // AuthAPI 전용 Retrofit (AuthClient 사용)
    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(@Named("AuthClient") authOkHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 일반 API UserApi 생성
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    // AuthAPI 생성 (AuthRetrofit 사용)
    @Provides
    @Singleton
    @Named("AuthApi")
    fun provideAuthApi(@Named("AuthRetrofit") authRetrofit: Retrofit): AuthApi {
        return authRetrofit.create(AuthApi::class.java)
    }

    // SharedPreferences 제공
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    //투표 api
    @Provides
    @Singleton
    fun provideVoteApi(retrofit: Retrofit): VoteApi {
        return retrofit.create(VoteApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // FULL 요청/응답 로깅
        }
    }
}



