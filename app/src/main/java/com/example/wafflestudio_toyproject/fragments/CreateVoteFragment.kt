package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.CreateVoteRequest
import com.example.wafflestudio_toyproject.CreateVoteResponse
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.databinding.FragmentCreateVoteBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class CreateVoteFragment : Fragment() {
    private var _binding: FragmentCreateVoteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var voteApi: VoteApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVoteBinding.inflate(inflater, container, false)

        // 투표 항목 추가 생성
        binding.buttonAddOption.setOnClickListener {
            addOption()
        }

        // 참여 코드 입력창 활성화
        binding.checkboxCreateParticipationCode.setOnCheckedChangeListener { _, isChecked ->
            binding.participationCodeInput.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // 투표글 생성
        binding.buttonSubmitVote.setOnClickListener {
            createVote()
        }

        return binding.root
    }
    
    // 투표 항목 추가
    private fun addOption() {
        val newOption = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "New Option"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        binding.optionsContainer.addView(newOption)
    }
    
    // 투표글 생성
    private fun createVote() {
        val title = binding.voteTitle.text.toString().trim()
        val content = binding.voteDescription.text.toString().trim()
        val participationCodeRequired = binding.checkboxCreateParticipationCode.isChecked
        val participationCode = if (participationCodeRequired) binding.participationCodeInput.text.toString() else null
        val realtimeResult = binding.checkboxRevealResults.isChecked
        val multipleChoice = binding.checkboxAllowDuplicates.isChecked
        val annonymousChoice = binding.checkboxAnonymous.isChecked
        val endDatetime = "${binding.deadlineDate.text}T${binding.deadlineTime.text}"
        val choices = (0 until binding.optionsContainer.childCount).mapNotNull { index ->
            val view = binding.optionsContainer.getChildAt(index) as? EditText
            view?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }

        // 입력값 검증
        val errorMessage = when {
            title.isEmpty() || title.length > 100 -> "Title must be between 1 and 100 characters."
            content.isEmpty() || content.length > 200 -> "Content must be between 1 and 200 characters."
            participationCodeRequired && (participationCode.isNullOrEmpty() || participationCode.length != 6) -> "Participation code must be 6 characters."
            choices.isEmpty() -> "At least one choice is required."
            binding.deadlineDate.text.isNullOrEmpty() || binding.deadlineTime.text.isNullOrEmpty() -> "Date and Time must not be empty."
            else -> null
        }

        if (errorMessage != null) {
            showError(errorMessage)
            return
        }

        // Prepare API request
        val request = CreateVoteRequest(
            title = title,
            content = content,
            participation_code_required = participationCodeRequired,
            participation_code = participationCode,
            realtime_result = realtimeResult,
            multiple_choice = multipleChoice,
            annonymous_choice = annonymousChoice,
            end_datetime = endDatetime,
            choices = choices
        )

        voteApi.createVote(request).enqueue(object : Callback<CreateVoteResponse> {
            override fun onResponse(
                call: Call<CreateVoteResponse>,
                response: Response<CreateVoteResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Vote created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    showError("Failed to create vote: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CreateVoteResponse>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })

        Log.d("ButtonDebug", "Button Visibility: ${binding.buttonSubmitVote.visibility}")

    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
        binding.errorTextView.requestLayout()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}