package com.example.wafflestudio_toyproject

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
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
    fun getMe(
        @Header("Authorization") authorization: String
    ): Call<GetMeResponse>

    @PATCH("api/users/reset_pw")
    fun changePassword(
        @Header("Authorization") authorization: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Call<ResponseBody>
}

interface AuthApi {
    @GET("/api/users/refresh")
    fun refreshToken(
        @Header("Authorization") authorization: String
    ): Call<RefreshTokenResponse>
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

    @GET("/api/votes/ongoing_list")
    fun getOngoingVotes(
        @Query("start_cursor") startCursor: String? = null,
        @Header("Authorization") token: String
    ): Call<OngoingVoteResponse>

    @GET("/api/votes/{vote_id}")
    fun getVoteDetails(
        @Path("vote_id") voteId: Int,
        @Header("Authorization") authToken: String
    ): Call<VoteDetailResponse>

    @POST("/api/votes/{vote_id}/participate")
    fun participateInVote(
        @Path("vote_id") voteId: Int,
        @Header("Authorization") authorization: String,
        @Body requestBody: ParticipationRequest
    ): Call<VoteDetailResponse>

    @POST("api/votes/{voteId}/comment")
    fun postComment(
        @Path("voteId") voteId: Int,
        @Header("Authorization") token: String,
        @Body commentRequest: CommentRequest
    ): Call<VoteDetailResponse>

    @PATCH("api/votes/{vote_id}/comment/{comment_id}")
    fun updateComment(
        @Path("vote_id") voteId: Int,
        @Path("comment_id") commentId: Int,
        @Header("Authorization") token: String,
        @Body request: CommentRequest
    ): Call<VoteDetailResponse>

    @DELETE("api/votes/{vote_id}/comment/{comment_id}")
    fun deleteComment(
        @Path("vote_id") voteId: Int,
        @Path("comment_id") commentId: Int,
        @Header("Authorization") token: String
    ): Call<VoteDetailResponse>
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

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String,
    val confirm_new_password: String
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

data class VoteDetailResponse(
    val vote_id: Int,
    val writer_name: String,
    val is_writer: Boolean,
    val title: String,
    val content: String,
    val participation_code_required: Boolean,
    val choices: List<Choice>,
    val comments: List<Comment>,
    val realtime_result: Boolean,
    val multiple_choice: Boolean,
    val annonymous_choice: Boolean,
    val create_datetime: String,
    val end_datetime: String,
    val images: List<String>,
    val participant_count: Int
){
    data class Choice(
        val choice_id: Int,
        val choice_content: String,
        val participated: Boolean,
        val choice_num_participants: Int?,
        val choice_participants_name: List<String>?
    )

    data class Comment(
        val comment_id: Int,
        val writer_name: String,
        val is_writer: Boolean,
        val comment_content: String,
        val created_datetime: String,
        val is_edited: Boolean,
        val edited_datetime: String?
    ){
        fun formatDatetime(isoDatetime: String): String {
            return try {
                val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) // 서버에서 오는 ISO 8601 형식
                val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())  // 표시할 형식
                val date = serverFormat.parse(isoDatetime) // 문자열을 Date 객체로 변환
                displayFormat.format(date!!) // Date 객체를 원하는 형식으로 변환 후 반환
            } catch (e: Exception) {
                "날짜 형식 오류"
            }
        }
    }

    fun calculateTimeRemaining(): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val endDate = format.parse(end_datetime) ?: return "시간 계산 불가"
            val now = Date()

            val diff = endDate.time - now.time
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = (diff / (1000 * 60 * 60)) % 24
            val minutes = (diff / (1000 * 60)) % 60

            when {
                diff <= 0 -> "종료됨"
                days > 0 -> "${days}일 ${hours}시간 남음"
                hours > 0 -> "${hours}시간 ${minutes}분 남음"
                else -> "${minutes}분 남음"
            }
        } catch (e: Exception) {
            "시간 계산 불가"
        }
    }
}

data class OngoingVoteResponse(
    val votes_list: List<VoteItem>,
    val has_next: Boolean,
    val next_cursor: String?
)

data class VoteItem(
    val id: Int,
    val title: String,
    val content: String,
    val create_datetime: String,
    val end_datetime: String,
    val participated: Boolean,
    val image: String?,
    val participantCount: Int
) {
    fun calculateTimeRemaining(): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val endDate = format.parse(end_datetime) ?: return "시간 계산 불가"
            val now = Date()

            val diff = endDate.time - now.time
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = (diff / (1000 * 60 * 60)) % 24
            val minutes = (diff / (1000 * 60)) % 60

            when {
                diff <= 0 -> "종료됨"
                days > 0 -> "${days}일 ${hours}시간 남음"
                hours > 0 -> "${hours}시간 ${minutes}분 남음"
                else -> "${minutes}분 남음"
            }
        } catch (e: Exception) {
            "시간 계산 불가"
        }
    }
}

data class ParticipationRequest(
    val participated_choice_ids: List<Int>,
    val participation_code: String? = null
)

data class CommentRequest(
    val content: String
)

data class GetMeResponse(
    val name: String,
    val userid: String,
    val email: String,
    val college: Int
)



