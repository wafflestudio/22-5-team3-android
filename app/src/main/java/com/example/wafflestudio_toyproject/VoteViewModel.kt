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

    fun fetchVotes() {
        voteRepository.getOngoingVotes().observeForever { votes ->
            _allVotes.value = votes
        }
    }
}