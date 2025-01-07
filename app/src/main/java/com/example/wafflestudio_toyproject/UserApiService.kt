package com.example.wafflestudio_toyproject

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @POST("api/users/signup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @POST("api/users/signin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}

interface AuthApi{
    @GET("api/users/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}

interface VoteApi {
    @POST("api/votes/create")
    fun createVote(@Body request: CreateVoteRequest): Call<CreateVoteResponse>
}

// 요청 데이터 클래스
data class SignupRequest(
    val userid: String,
    val email: String,
    val password: String,
    val name: String,
    val college: Int
)

data class LoginRequest(
    val userid: String,
    val password: String
)

data class CreateVoteRequest(
    val title: String,
    val content: String,
    val participation_code_required: Boolean,
    val participation_code: String?,
    val realtime_result: Boolean,
    val multiple_choice: Boolean,
    val annonymous_choice: Boolean,
    val end_datetime: String,
    val choices: List<String>
)

// 응답 데이터 클래스
data class SignupResponse(
    val id: String,
    val email: String
)

data class LoginResponse(
    val access_token: String,
    val refresh_token: String
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class RefreshTokenResponse(
    val access_token: String,
    val refresh_token: String
)

data class CreateVoteResponse(
    val id: Int,
    val title: String,
    val content: String,
    val choices: List<Choice>,
    val participation_code: String?,
    val realtime_result: Boolean,
    val multiple_choice: Boolean,
    val annonymous_choice: Boolean,
    val create_datetime: String,
    val end_datetime: String
) {
    data class Choice(
        val vote_id: Int,
        val choice_content: String,
        val id: Int
    )
}


