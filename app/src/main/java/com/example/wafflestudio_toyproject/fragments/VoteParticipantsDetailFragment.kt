package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.VoteDetailResponse
import com.example.wafflestudio_toyproject.databinding.FragmentVoteParticipantsDetailBinding

class VoteParticipantsDetailFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentVoteParticipantsDetailBinding? = null
    private val binding get() = _binding!!

    private var voteId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteParticipantsDetailBinding.inflate(inflater, container, false)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("vote_id", voteId) // voteId를 다시 전달
            }
            navController.navigate(
                R.id.action_voteParticipantsDetailFragment_to_voteDetailFragment,
                bundle
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        voteId = arguments?.getInt("vote_id", -1) ?: -1

        // Bundle에서 choices 데이터 복원
        val choices: List<VoteDetailResponse.Choice> = arguments?.getParcelableArrayList<Bundle>("choices")
            ?.map { bundle ->
                VoteDetailResponse.Choice(
                    choice_id = bundle.getInt("choice_id"),
                    choice_content = bundle.getString("choice_content") ?: "",
                    participated = bundle.getBoolean("participated"),
                    choice_num_participants = bundle.getInt("choice_num_participants"),
                    choice_participants_name = bundle.getStringArrayList("choice_participants_name")
                )
            } ?: emptyList()

        // 참여자 목록 표시
        displayParticipants(choices)
    }

    private fun displayParticipants(choices: List<VoteDetailResponse.Choice>) {
        val participantText = StringBuilder()
        choices.forEach { choice ->
            participantText.append("${choice.choice_content}: ${choice.choice_num_participants}명 \n")
            val participants = choice.choice_participants_name ?: listOf("참여자 없음")
            participantText.append(participants.joinToString(", "))
            participantText.append("\n\n")
        }
        binding.participantList.text = participantText.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

