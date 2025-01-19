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

    private var nextCursor: String? = null
    private var isLoading = false

    fun fetchVotes() {
        if (isLoading) return  // 이미 요청 중이면 중복 요청 방지
        isLoading = true

        voteRepository.getOngoingVotes(nextCursor).observeForever { response ->
            response?.let {
                val updatedList = _allVotes.value?.toMutableList() ?: mutableListOf()
                updatedList.addAll(it.votes_list)

                _allVotes.value = updatedList
                nextCursor = if (it.has_next) it.next_cursor else null
            }
            isLoading = false
        }
    }

    // 다음 페이지 요청
    fun loadMoreVotes() {
        nextCursor?.let { fetchVotes() }
    }
}