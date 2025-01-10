package com.example.wafflestudio_toyproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.wafflestudio_toyproject.adapter.ViewPagerAdapter
import com.example.wafflestudio_toyproject.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(this)

        setTabLayout()
    }

    private fun setTabLayout() {
        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position){
                0->{
                    tab.text = "진행 중인 투표"
                    tab.setIcon(R.drawable.ongoing_vote)
                }
                1->{
                    tab.text = "Hot 투표"
                    tab.setIcon(R.drawable.hot_vote)
                }
                2-> {
                    tab.text = "종료된 투표"
                    tab.setIcon(R.drawable.vote_result)
                }
            }
        }.attach()
    }
}