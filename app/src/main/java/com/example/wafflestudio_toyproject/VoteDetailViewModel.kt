package com.example.wafflestudio_toyproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteDetailViewModel @Inject constructor(private val repository: VoteDetailRepository) : ViewModel() {
    private val _voteDetails = MutableLiveData<Result<VoteDetailResponse>>()
    val voteDetails: LiveData<Result<VoteDetailResponse>> get() = _voteDetails

    private val _voteResult = MutableLiveData<Result<VoteDetailResponse>>()
    val voteResult: LiveData<Result<VoteDetailResponse>> get() = _voteResult

    private val _selectedChoices = MutableLiveData<MutableSet<Int>>(mutableSetOf())
    val selectedChoices: LiveData<Set<Int>> get() = _selectedChoices.map { it.toSet() }

    private val _commentResult = MutableLiveData<Result<VoteDetailResponse>>()
    val commentResult: LiveData<Result<VoteDetailResponse>> get() = _commentResult

    //투표 정보 불러오기
    fun fetchVoteDetails(voteId: Int, accessToken: String) {
        repository.getVoteDetails(voteId, accessToken) { result ->
            _voteDetails.postValue(result) // LiveData 업데이트
        }
    }

    //투표 참여하기
    fun performVote(voteId: Int, accessToken: String, selectedChoices: List<Int>, enteredCode: String?) {
        val participationRequest = ParticipationRequest(
            participated_choice_ids = selectedChoices,
            participation_code = enteredCode
        )

        repository.participateInVote(voteId, accessToken, participationRequest) { result ->
            _voteResult.postValue(result) // UI 업데이트를 위해 LiveData에 결과 저장
        }
    }

    //투표 선택지 선택 관리
    fun setInitialChoices(choices: List<VoteDetailResponse.Choice>) {
        val initialChoices = choices.filter { it.participated }.map { it.choice_id }.toMutableSet()
        _selectedChoices.value = initialChoices
    }

    fun toggleChoice(choiceId: Int, isMultipleChoice: Boolean) {
        val currentChoices = _selectedChoices.value ?: mutableSetOf()

        if (isMultipleChoice) {
            if (currentChoices.contains(choiceId)) {
                currentChoices.remove(choiceId)
            } else {
                currentChoices.add(choiceId)
            }
        } else {
            currentChoices.clear()
            currentChoices.add(choiceId)
        }

        _selectedChoices.value = currentChoices
    }

    fun clearChoices() {
        _selectedChoices.value = mutableSetOf()
    }

    //댓글 관리
    fun postComment(voteId: Int, content: String, accessToken: String) {
        repository.postComment(voteId, content, accessToken) { result ->
            _commentResult.postValue(result)
        }
    }

    fun editComment(voteId: Int, commentId: Int, updatedContent: String, accessToken: String) {
        repository.editComment(voteId, commentId, updatedContent, accessToken) { result ->
            _commentResult.postValue(result)
        }
    }

    fun deleteComment(voteId: Int, commentId: Int, accessToken: String) {
        repository.deleteComment(voteId, commentId, accessToken) { result ->
            _commentResult.postValue(result)
        }
    }
}