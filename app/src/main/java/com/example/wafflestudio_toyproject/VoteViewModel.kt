package com.example.wafflestudio_toyproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val _allVotes = MutableLiveData<List<VoteItem>>()
    val allVotes: LiveData<List<VoteItem>> get() = _allVotes

    private var nextCursorOngoing: String? = null
    private var nextCursorEnded: String? = null
    private var nextCursorHot: String? = null
    private var isLoading = false

    fun fetchOngoingVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorOngoing = null
        }

        voteRepository.getVotes("ongoing", nextCursorOngoing).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }
                nextCursorOngoing = if (it.has_next) it.next_cursor else null
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreOngoingVotes() {
        nextCursorOngoing?.let { fetchOngoingVotes() }
    }

    fun fetchEndedVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorEnded = null
        }

        voteRepository.getVotes("ended", nextCursorEnded).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                nextCursorEnded = if (it.has_next) it.next_cursor else null
            }
            isLoading = false
        }
    }

    // 참여한 투표
    fun fetchParticipatedVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorEnded = null
        }

        voteRepository.getVotes("participated", nextCursorEnded).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                nextCursorEnded = if (it.has_next) it.next_cursor else null
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreEndedVotes() {
        nextCursorEnded?.let { fetchEndedVotes() }
    }

    fun fetchHotVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorHot = null
        }

        voteRepository.getVotes("hot", nextCursorHot).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                nextCursorHot = if (it.has_next) it.next_cursor else null
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreHotVotes() {
        nextCursorHot?.let { fetchEndedVotes() }
    }

    fun addNewVote(newVote: VoteItem) {
        val updatedList = (_allVotes.value?.toMutableList() ?: mutableListOf())
        updatedList.add(0, newVote)  // ✅ 새 투표를 리스트 맨 앞에 추가
        _allVotes.value = updatedList  // ✅ LiveData 업데이트
    }
}