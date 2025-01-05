package com.example.wafflestudio_toyproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wafflestudio_toyproject.databinding.FragmentHotvoteBinding

class HotVoteFragment : Fragment() {
    private var _binding: FragmentHotvoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotvoteBinding.inflate(inflater, container, false)

        /*
        // NavHostFragment 초기화
        val navHostFragment = childFragmentManager.findFragmentById(R.id.app_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

         */

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}