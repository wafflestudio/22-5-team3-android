package com.example.wafflestudio_toyproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wafflestudio_toyproject.network.VoteApi
import com.example.wafflestudio_toyproject.network.VoteListResponse
import jakarta.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteApi: VoteApi,
    private val userRepository: UserRepository
) {
    fun getVotes(category: String?,startCursorTime: String?, startCursorId: Int?): LiveData<VoteListResponse?> {
        val liveData = MutableLiveData<VoteListResponse?>()
        val accessToken = userRepository.getAccessToken()

        voteApi.getVotes(startCursorTime, startCursorId, category).enqueue(object : Callback<VoteListResponse> {
            override fun onResponse(call: Call<VoteListResponse>, response: Response<VoteListResponse>) {
                if (response.isSuccessful) {
                    liveData.postValue(response.body())
                } else {
                    liveData.postValue(null) // 실패 시 빈 리스트 반환
                }
            }

            override fun onFailure(call: Call<VoteListResponse>, t: Throwable) {
                liveData.postValue(null)
            }
        })
        return liveData
    }
}
