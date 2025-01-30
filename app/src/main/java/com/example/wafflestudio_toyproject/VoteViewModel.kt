package com.example.wafflestudio_toyproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wafflestudio_toyproject.network.VoteItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val _allVotes = MutableLiveData<List<VoteItem>>()
    val allVotes: LiveData<List<VoteItem>> get() = _allVotes

    private var nextCursorOngoingTime: String? = null
    private var nextCursorOngoingId: Int? = null

    private var nextCursorEndedTime: String? = null
    private var nextCursorEndedId: Int? = null

    private var nextCursorHotTime: String? = null
    private var nextCursorHotId: Int? = null

    private var isLoading = false

    fun fetchOngoingVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorOngoingTime = null
            nextCursorOngoingId = null
        }

        voteRepository.getVotes("ongoing", nextCursorOngoingTime, nextCursorOngoingId).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }
                if (it.has_next) {
                    nextCursorOngoingTime = it.next_cursor_time
                    nextCursorOngoingId = it.next_cursor_id
                } else {
                    nextCursorOngoingTime = null
                    nextCursorOngoingId = null
                }
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreOngoingVotes() {
        if (nextCursorOngoingTime != null && nextCursorOngoingId != null) {
            fetchOngoingVotes()
        }
    }

    fun fetchEndedVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorEndedTime = null
            nextCursorEndedId = null
        }

        voteRepository.getVotes("ended", nextCursorEndedTime, nextCursorEndedId).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                if (it.has_next) {
                    nextCursorEndedTime = it.next_cursor_time
                    nextCursorEndedId = it.next_cursor_id
                } else {
                    nextCursorEndedTime = null
                    nextCursorEndedId = null
                }
            }
            isLoading = false
        }
    }

    // 참여한 투표
    fun fetchParticipatedVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorOngoingTime = null
            nextCursorOngoingId = null
        }

        voteRepository.getVotes("participated", nextCursorOngoingTime, nextCursorOngoingId).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                if (it.has_next) {
                    nextCursorOngoingTime = it.next_cursor_time
                    nextCursorOngoingId = it.next_cursor_id
                } else {
                    nextCursorOngoingTime = null
                    nextCursorOngoingId = null
                }
            }
            isLoading = false
        }
    }

    fun fetchCreatedVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorOngoingTime = null
            nextCursorOngoingId = null
        }

        voteRepository.getVotes("made", nextCursorOngoingTime, nextCursorOngoingId).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                if (it.has_next) {
                    nextCursorOngoingTime = it.next_cursor_time
                    nextCursorOngoingId = it.next_cursor_id
                } else {
                    nextCursorOngoingTime = null
                    nextCursorOngoingId = null
                }
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreEndedVotes() {
        if (nextCursorEndedTime != null && nextCursorEndedId != null) {
            fetchEndedVotes()
        }
    }

    fun fetchHotVotes(isRefreshing: Boolean = false) {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        if (isRefreshing) {
            nextCursorHotTime = null
            nextCursorHotId = null
        }

        voteRepository.getVotes("hot", nextCursorHotTime, nextCursorHotId).observeForever { response ->
            response?.let {
                val updatedList = if (isRefreshing) {
                    it.votes_list
                } else {
                    (_allVotes.value?.toMutableList() ?: mutableListOf()) + it.votes_list
                }

                _allVotes.value = updatedList.distinctBy { vote -> vote.id }  // 중복 제거
                if (it.has_next) {
                    nextCursorHotTime = it.next_cursor_time
                    nextCursorHotId = it.next_cursor_id
                } else {
                    nextCursorHotTime = null
                    nextCursorHotId = null
                }
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreHotVotes() {
        if (nextCursorHotTime != null && nextCursorHotId != null) {
            fetchHotVotes()
        }
    }

    fun addNewVote(newVote: VoteItem) {
        val updatedList = (_allVotes.value?.toMutableList() ?: mutableListOf())
        updatedList.add(0, newVote)  // ✅ 새 투표를 리스트 맨 앞에 추가
        _allVotes.value = updatedList  // ✅ LiveData 업데이트
    }
}