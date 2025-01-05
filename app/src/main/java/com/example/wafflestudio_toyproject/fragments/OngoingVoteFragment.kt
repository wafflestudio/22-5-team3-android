package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.databinding.FragmentOngoingvoteBinding


class OngoingVoteFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentOngoingvoteBinding? = null
    private val binding get() = _binding!!

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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}