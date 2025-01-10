package com.example.wafflestudio_toyproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    fun getOngoingVotes(): LiveData<List<VoteItem>> {
        val liveData = MutableLiveData<List<VoteItem>>()
        val accessToken = userRepository.getAccessToken()

        voteApi.getOngoingVotes("Bearer $accessToken").enqueue(object : Callback<OngoingVoteResponse> {
            override fun onResponse(call: Call<OngoingVoteResponse>, response: Response<OngoingVoteResponse>) {
                if (response.isSuccessful) {
                    liveData.postValue(response.body()?.votes_list ?: emptyList())
                } else {
                    liveData.postValue(emptyList()) // 실패 시 빈 리스트 반환
                }
            }

            override fun onFailure(call: Call<OngoingVoteResponse>, t: Throwable) {
                liveData.postValue(emptyList())
            }
        })
        return liveData
    }
}
