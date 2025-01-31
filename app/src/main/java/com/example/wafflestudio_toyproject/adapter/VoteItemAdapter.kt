package com.example.wafflestudio_toyproject.adapter

import android.util.Log
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
            binding.voteTimeRemaining.text = voteItem.calculateTimeRemaining()
            binding.participateNumber.text = voteItem.participant_count.toString()

            if (!voteItem.image.isNullOrEmpty())
                binding.postImage.load(voteItem.image)

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
        holder.setIsRecyclable(false)
        holder.bind(voteItems[position])
    }

    fun updateItems(newItems: List<VoteItem>) {
        voteItems = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = voteItems.size
}

