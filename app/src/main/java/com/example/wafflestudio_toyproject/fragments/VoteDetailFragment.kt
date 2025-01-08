package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        if (voteDetail.participation_code_required) {
            binding.participationCodeLayout.visibility = View.VISIBLE
            binding.voteButton.isEnabled = false
        } else {
            binding.participationCodeLayout.visibility = View.GONE
            binding.voteButton.isEnabled = true
        }

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

        binding.validateCodeButton.setOnClickListener {
            val code = binding.participationCodeInput.text.toString()
            if (code.isNotEmpty()) {
                if(voteDetail.participation_code==code){
                    binding.voteButton.isEnabled = true
                    Toast.makeText(requireContext(), "참여 코드가 확인되었습니다.", Toast.LENGTH_SHORT).show()
                } else{
                    val correct_code=voteDetail.participation_code
                    Log.d("VoteDetail", "Code: $correct_code")
                    Toast.makeText(requireContext(), "참여 코드가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "참여 코드를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }


        // 투표 선택지 표시하기
        val selectedChoices = mutableSetOf<Int>() // 선택된 choice_id 저장

        voteDetail.choices.forEach { choice ->
            val button = Button(requireContext()).apply {
                text = choice.choice_content
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
    }

}