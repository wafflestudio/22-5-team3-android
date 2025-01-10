package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wafflestudio_toyproject.OngoingVoteResponse
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.VoteItem
import com.example.wafflestudio_toyproject.adapter.VoteItemAdapter
import com.example.wafflestudio_toyproject.databinding.FragmentOngoingvoteBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class OngoingVoteFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentOngoingvoteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var voteApi: VoteApi

    private lateinit var adapter: VoteItemAdapter
    private val voteItems = mutableListOf<VoteItem>()

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOngoingvoteBinding.inflate(inflater, container, false)

        // 투표 생성 페이지 연결
        binding.createVoteIcon.setOnClickListener {
            navController.navigate(R.id.action_ongoingVoteFragment_to_createVoteFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        // RecyclerView 설정
        adapter = VoteItemAdapter(voteItems) { voteItem ->
            val bundle = Bundle().apply {
                putInt("vote_id", voteItem.id)
            }
            navController.navigate(R.id.action_ongoingVoteFragment_to_voteDetailFragment, bundle)
        }
        binding.voteItemRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.voteItemRecyclerView.adapter = adapter

        // API 호출
        fetchOngoingVotes()
    }

    private fun fetchOngoingVotes(cursor: String? = null) {
        val accessToken = userRepository.getAccessToken()

        voteApi.getOngoingVotes("Bearer $accessToken").enqueue(object : Callback<OngoingVoteResponse> {
            override fun onResponse(
                call: Call<OngoingVoteResponse>,
                response: Response<OngoingVoteResponse>
            ) {
                if (response.isSuccessful) {
                    val ongoingVoteResponse = response.body()
                    if (ongoingVoteResponse != null) {
                        voteItems.addAll(ongoingVoteResponse.votes_list)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch votes: ${response.message()}", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<OngoingVoteResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}