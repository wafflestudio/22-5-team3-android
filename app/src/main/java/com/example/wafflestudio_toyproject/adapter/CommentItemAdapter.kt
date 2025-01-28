package com.example.wafflestudio_toyproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wafflestudio_toyproject.network.VoteDetailResponse
import com.example.wafflestudio_toyproject.databinding.CommentItemBinding

class CommentItemAdapter(
    private val comments: MutableList<VoteDetailResponse.Comment>,
    private val onEditComment: (VoteDetailResponse.Comment) -> Unit,
    private val onDeleteComment: (VoteDetailResponse.Comment) -> Unit
) : RecyclerView.Adapter<CommentItemAdapter.CommentViewHolder>() {
    inner class CommentViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: VoteDetailResponse.Comment){
            binding.writerName.text = if (comment.writer_name=="") "String" else comment.writer_name
            binding.commentContent.text = comment.comment_content
            binding.createDatetime.text = comment.formatDatetime(comment.created_datetime)

            // 수정된 시간 표시
            if (comment.is_edited && comment.edited_datetime != null) {
                val editedDate = comment.formatDatetime(comment.edited_datetime)
                binding.editedFlag.text = "($editedDate 편집됨)"
                binding.editedFlag.visibility = View.VISIBLE
            } else {
                binding.editedFlag.visibility = View.GONE
            }

            // 댓글 수정 버튼 클릭 리스너
            if (comment.is_writer) {
                binding.editButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.VISIBLE
                binding.divider2.visibility = View.VISIBLE
            } else {
                binding.editButton.visibility = View.GONE
                binding.deleteButton.visibility = View.GONE
                binding.divider2.visibility = View.GONE
            }

            binding.editButton.setOnClickListener { onEditComment(comment) }
            binding.deleteButton.setOnClickListener { onDeleteComment(comment) }
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