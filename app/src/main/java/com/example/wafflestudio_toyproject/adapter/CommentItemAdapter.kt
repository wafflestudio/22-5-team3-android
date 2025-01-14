package com.example.wafflestudio_toyproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wafflestudio_toyproject.VoteDetailResponse
import com.example.wafflestudio_toyproject.databinding.CommentItemBinding

class CommentItemAdapter(private val comments: MutableList<VoteDetailResponse.Comment>) :
    RecyclerView.Adapter<CommentItemAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: VoteDetailResponse.Comment){
            binding.writerName.text = if (comment.writer_name=="") "String" else comment.writer_name
            binding.commentContent.text = comment.comment_content
            binding.createDatetime.text = comment.formatDatetime(comment.created_datetime)
            binding.editedFlag.visibility = if (comment.is_edited) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size
}