package com.example.wafflestudio_toyproject.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface UserApi {
    @POST("api/users/signup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @POST("api/users/signin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("api/users/me")
    fun getMe(): Call<GetMeResponse>

    @PATCH("api/users/reset_pw")
    fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Call<ResponseBody>
}

interface AuthApi {
    @GET("/api/users/refresh")
    fun refreshToken(): Call<RefreshTokenResponse>
}

interface VoteApi {
    @Multipart
    @POST("api/votes/create")
    fun createVoteWithImage(
        @Part("create_vote_json") createVoteJson: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Call<CreateVoteResponse>

    @Multipart
    @POST("api/votes/create")
    fun createVoteWithoutImage(
        @Part("create_vote_json") createVoteJson: RequestBody
    ): Call<CreateVoteResponse>

    @GET("/api/votes/list")
    fun getVotes(
        @Query("start_cursor") startCursor: String? = null,
        @Query("category") category: String? = null
    ): Call<VoteListResponse>

    @GET("/api/votes/{vote_id}")
    fun getVoteDetails(
        @Path("vote_id") voteId: Int
    ): Call<VoteDetailResponse>

    @POST("/api/votes/{vote_id}/participate")
    fun participateInVote(
        @Path("vote_id") voteId: Int,
        @Body requestBody: ParticipationRequest
    ): Call<VoteDetailResponse>

    @POST("api/votes/{voteId}/comment")
    fun postComment(
        @Path("voteId") voteId: Int,
        @Body commentRequest: CommentRequest
    ): Call<VoteDetailResponse>

    @PATCH("api/votes/{vote_id}/comment/{comment_id}")
    fun updateComment(
        @Path("vote_id") voteId: Int,
        @Path("comment_id") commentId: Int,
        @Body request: CommentRequest
    ): Call<VoteDetailResponse>

    @DELETE("api/votes/{vote_id}/comment/{comment_id}")
    fun deleteComment(
        @Path("vote_id") voteId: Int,
        @Path("comment_id") commentId: Int
    ): Call<VoteDetailResponse>
}




