package com.example.wafflestudio_toyproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wafflestudio_toyproject.VoteItem
import com.example.wafflestudio_toyproject.databinding.VoteItemBinding

class VoteItemAdapter(
    private val voteItems: List<VoteItem>,
    private val onClick: (VoteItem) -> Unit
) : RecyclerView.Adapter<VoteItemAdapter.VoteViewHolder>() {

    inner class VoteViewHolder(private val binding: VoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(voteItem: VoteItem) {
            binding.voteTitle.text = voteItem.title
            binding.voteTimeRemaining.text = voteItem.calculateTimeRemaining()

            binding.root.setOnClickListener {
                onClick(voteItem)
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

    override fun getItemCount(): Int = voteItems.size
}

