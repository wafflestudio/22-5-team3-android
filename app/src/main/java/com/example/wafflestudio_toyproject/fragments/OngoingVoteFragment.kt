package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wafflestudio_toyproject.OngoingVoteResponse
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.UserRepository
import com.example.wafflestudio_toyproject.VoteApi
import com.example.wafflestudio_toyproject.VoteItem
import com.example.wafflestudio_toyproject.VoteViewModel
import com.example.wafflestudio_toyproject.adapter.VoteItemAdapter
import com.example.wafflestudio_toyproject.databinding.FragmentOngoingvoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    private val voteViewModel: VoteViewModel by viewModels()

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
        binding.voteItemRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItemPosition >= totalItemCount - 2) {  // 마지막에서 두 번째 아이템이 보이면 로드
                    lifecycleScope.launch {
                        voteViewModel.loadMoreVotes()
                    }
                }
            }
        })

        // API 호출
        voteViewModel.allVotes.observe(viewLifecycleOwner) { allVotes ->
            adapter.updateItems(allVotes)
        }

        lifecycleScope.launch {
            voteViewModel.fetchVotes()
        }

        voteViewModel.allVotes.observe(viewLifecycleOwner) { allVotes ->
            val updatedVotes = allVotes.map { voteItem ->
                voteItem.copy(participated = voteItem.participated ) // 사용자가 선택한 항목이 있는지 확인
            }
            adapter.updateItems(updatedVotes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}