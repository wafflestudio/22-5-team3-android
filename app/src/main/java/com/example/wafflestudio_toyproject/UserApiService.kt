package com.example.wafflestudio_toyproject

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("api/users/signup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>
}

// 요청 데이터 클래스
data class SignupRequest(
    val userid: String,
    val email: String,
    val password: String,
    val name: String,
    val department: Int
)

// 응답 데이터 클래스
data class SignupResponse(
    val id: String,
    val email: String
)
