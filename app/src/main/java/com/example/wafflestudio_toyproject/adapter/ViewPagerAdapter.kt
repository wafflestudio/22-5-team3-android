package com.example.wafflestudio_toyproject.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wafflestudio_toyproject.fragments.HotVoteFragment
import com.example.wafflestudio_toyproject.fragments.UserProfileFragment
import com.example.wafflestudio_toyproject.fragments.VoteFragment
import com.example.wafflestudio_toyproject.fragments.VoteResultFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VoteFragment()
            1 -> HotVoteFragment()
            2 -> VoteResultFragment()
            3 -> UserProfileFragment()

            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}