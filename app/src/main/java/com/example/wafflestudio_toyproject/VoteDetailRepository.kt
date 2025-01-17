package com.example.wafflestudio_toyproject

import jakarta.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class VoteDetailRepository @Inject constructor(
    private val voteApi: VoteApi,
    private val userRepository: UserRepository
) {
    fun getVoteDetails(voteId: Int, accessToken: String, callback: (Result<VoteDetailResponse>) -> Unit) {
        voteApi.getVoteDetails(voteId, "Bearer $accessToken").enqueue(object : Callback<VoteDetailResponse> {
            override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { callback(Result.success(it)) }
                        ?: callback(Result.failure(Exception("Response body is null")))
                } else {
                    callback(Result.failure(Exception("Failed to fetch vote details: ${response.message()}")))
                }
            }

            override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun participateInVote(
        voteId: Int,
        accessToken: String,
        participationRequest: ParticipationRequest,
        callback: (Result<VoteDetailResponse>) -> Unit
    ) {
        voteApi.participateInVote(voteId, "Bearer $accessToken", participationRequest)
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { callback(Result.success(it)) }
                            ?: callback(Result.failure(Exception("Response body is null")))
                    } else {
                        callback(Result.failure(Exception(response.message())))
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    callback(Result.failure(t))
                }
            })
    }

    fun postComment(voteId: Int, content: String, accessToken: String, callback: (Result<VoteDetailResponse>) -> Unit) {
        val commentRequest = CommentRequest(content)

        voteApi.postComment(voteId, "Bearer $accessToken", commentRequest)
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { callback(Result.success(it)) }
                            ?: callback(Result.failure(Exception("Response body is null")))
                    } else {
                        callback(Result.failure(Exception("Failed to post comment: ${response.message()}")))
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    callback(Result.failure(t))
                }
            })
    }

    fun editComment(voteId: Int, commentId: Int, updatedContent: String, accessToken: String, callback: (Result<VoteDetailResponse>) -> Unit) {
        val commentRequest = CommentRequest(updatedContent)

        voteApi.updateComment(voteId, commentId, "Bearer $accessToken", commentRequest)
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { callback(Result.success(it)) }
                            ?: callback(Result.failure(Exception("Response body is null")))
                    } else {
                        callback(Result.failure(Exception("Failed to edit comment: ${response.message()}")))
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    callback(Result.failure(t))
                }
            })
    }

    fun deleteComment(voteId: Int, commentId: Int, accessToken: String, callback: (Result<VoteDetailResponse>) -> Unit) {
        voteApi.deleteComment(voteId, commentId, "Bearer $accessToken")
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { callback(Result.success(it)) }
                            ?: callback(Result.failure(Exception("Response body is null")))
                    } else {
                        callback(Result.failure(Exception("Failed to delete comment: ${response.message()}")))
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    callback(Result.failure(t))
                }
            })
    }
}