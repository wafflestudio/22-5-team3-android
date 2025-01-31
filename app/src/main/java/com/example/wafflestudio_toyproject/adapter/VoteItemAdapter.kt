package com.example.wafflestudio_toyproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.network.VoteItem
import com.example.wafflestudio_toyproject.databinding.VoteItemBinding

class VoteItemAdapter(
    private var voteItems: List<VoteItem>,
    private val onClick: (VoteItem, Boolean) -> Unit,
    private val isBackgroundFixed: Boolean = false
) : RecyclerView.Adapter<VoteItemAdapter.VoteViewHolder>() {

    inner class VoteViewHolder(private val binding: VoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(voteItem: VoteItem) {
            binding.voteTitle.text = voteItem.title
            binding.participateNumber.text = voteItem.participant_count.toString()

            // 종료된 경우 "종료됨", 그렇지 않으면 남은 시간 표시
            binding.voteTimeRemaining.text = if (voteItem.isEnded()) {
                "종료됨"
            } else {
                voteItem.calculateTimeRemaining()
            }

            // 이미지 로드
            voteItem.image?.let {
                binding.postImage.load(it)
            }

            binding.root.setOnClickListener {
                onClick(voteItem, voteItem.isEnded())
            }

            if (voteItem.participated && !isBackgroundFixed) {
                binding.voteCardView.setBackgroundResource(R.drawable.participate_vote)
            } else {
                binding.voteCardView.setBackgroundResource(R.drawable.rounded_rectangle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val binding = VoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(voteItems[position])
    }

    fun updateItems(newItems: List<VoteItem>) {
        voteItems = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = voteItems.size
}

