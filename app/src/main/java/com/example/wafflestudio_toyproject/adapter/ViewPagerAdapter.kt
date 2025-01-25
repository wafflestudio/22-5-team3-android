package com.example.wafflestudio_toyproject.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wafflestudio_toyproject.fragments.EndedVoteFragmentHost
import com.example.wafflestudio_toyproject.fragments.HotVoteFragmentHost
import com.example.wafflestudio_toyproject.fragments.UserFragment
import com.example.wafflestudio_toyproject.fragments.OngoingVoteFragmentHost

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingVoteFragmentHost()
            1 -> HotVoteFragmentHost()
            2 -> EndedVoteFragmentHost()
            3 -> UserFragment()

            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}