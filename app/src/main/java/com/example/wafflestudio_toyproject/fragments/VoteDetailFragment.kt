package com.example.wafflestudio_toyproject.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

import com.example.wafflestudio_toyproject.ParticipationRequest

import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.VoteDetailResponse
import com.example.wafflestudio_toyproject.databinding.FragmentVoteDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class VoteDetailFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentVoteDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var voteApi: VoteApi

    @Inject
    lateinit var userRepository: UserRepository

    private var voteId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteDetailBinding.inflate(inflater, container, false)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            navController.navigate(R.id.action_voteDetailFragment_to_ongoingVoteFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        // 전달된 vote_id 가져오기
        arguments?.let {
            voteId = it.getInt("vote_id", -1)
        }

        if (voteId != -1) {
            fetchVoteDetails(voteId) // API 호출
        } else {
            Toast.makeText(requireContext(), "Invalid vote ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchVoteDetails(voteId: Int) {
        val accessToken = userRepository.getAccessToken()

        voteApi.getVoteDetails(voteId, "Bearer $accessToken")
            .enqueue(object : Callback<VoteDetailResponse> {
                override fun onResponse(
                    call: Call<VoteDetailResponse>,
                    response: Response<VoteDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { voteDetail ->
                            displayVoteDetails(voteDetail)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch vote details: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayVoteDetails(voteDetail: VoteDetailResponse) {
        binding.voteDetailTitle.text = voteDetail.title
        binding.voteDetailDescription.text = voteDetail.content

        if (voteDetail.multiple_choice) {
            binding.multipleChoiceMessage.visibility = View.VISIBLE
        } else {
            binding.multipleChoiceMessage.visibility = View.GONE
        }

        if (voteDetail.annonymous_choice) {
            binding.anonymousMessage.visibility = View.VISIBLE
        } else {
            binding.anonymousMessage.visibility = View.GONE
        }

        val hasParticipated = voteDetail.choices.any { it.participated }

        if (hasParticipated) { // 투표 여부를 서버에서 반환하는 경우
            binding.voteButton.text = "다시 투표하기"
        } else {
            binding.voteButton.text = "투표하기"
        }



        // 투표 선택지 표시하기
        val selectedChoices = mutableSetOf<Int>() // 선택된 choice_id 저장

        binding.choicesContainer.removeAllViews()
        voteDetail.choices.forEach { choice ->
            val button = Button(requireContext()).apply {
                text = if (voteDetail.realtime_result) {
                    "${choice.choice_content} - ${choice.choice_num_participants ?: 0}명 참여"
                } else {
                    choice.choice_content
                }
                isEnabled = true
                setBackgroundResource(R.drawable.vote_button_selector)

                //버튼 스타일 지정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    120
                ).apply {
                    setMargins(0, 0, 0, 16)
                }

                // 선택 상태 초기화
                isSelected = choice.participated

                setOnClickListener {
                    if (voteDetail.multiple_choice) {
                        // 다중 선택 가능
                        if (selectedChoices.contains(choice.choice_id)) {
                            selectedChoices.remove(choice.choice_id)
                            isSelected = false
                        } else {
                            selectedChoices.add(choice.choice_id)
                            isSelected = true
                        }
                    } else {
                        // 단일 선택: 기존 선택 해제
                        selectedChoices.clear()
                        selectedChoices.add(choice.choice_id)
                        binding.choicesContainer.forEach { child ->
                            if (child is Button) child.isSelected = false
                        }
                        isSelected = true
                    }
                }
            }
            binding.choicesContainer.addView(button)
        }


        fun showParticipationCodeDialog(onCodeEntered: (String) -> Unit) {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_participation_code, null)
            val codeInput = dialogView.findViewById<EditText>(R.id.participationCodeInput)

            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ ->
                    val enteredCode = codeInput.text.toString()
                    if (enteredCode.isNotEmpty()) {
                        onCodeEntered(enteredCode)
                    } else {
                        binding.errorTextView.text = "참여 코드를 입력하세요."
                        binding.errorTextView.visibility = View.VISIBLE
                    }
                }
                .create()
                .show()
        }

        fun performVote(enteredCode: String?) {
            val accessToken = userRepository.getAccessToken()
            val participationRequest = ParticipationRequest(
                participated_choice_ids = selectedChoices.toList(),
                participation_code = enteredCode
            )

            voteApi.participateInVote(voteId, "Bearer $accessToken", participationRequest)
                .enqueue(object : Callback<VoteDetailResponse> {
                    override fun onResponse(call: Call<VoteDetailResponse>, response: Response<VoteDetailResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "투표에 성공적으로 참여했습니다!", Toast.LENGTH_SHORT).show()
                            binding.errorTextView.visibility = View.GONE
                            fetchVoteDetails(voteId)
                            binding.voteButton.text = "다시 투표하기"
                        } else {
                            if(response.message() == "Forbidden") {
                                binding.errorTextView.text = "참여 코드가 틀렸습니다."
                                binding.errorTextView.visibility = View.VISIBLE
                            }
                            //Toast.makeText(requireContext(), "투표 참여 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<VoteDetailResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // voteButton 클릭 이벤트 추가
        binding.voteButton.setOnClickListener {
            if (selectedChoices.toList().isEmpty()){
                binding.errorTextView.text = "투표 항목을 선택해주세요."
                binding.errorTextView.visibility = View.VISIBLE
            } else {
                if (voteDetail.participation_code_required) {
                    showParticipationCodeDialog { enteredCode ->
                        performVote(enteredCode)
                    }
                } else {
                    performVote(null) // 참여 코드가 필요 없는 경우 바로 투표 진행
                }
            }
        }
    }
}