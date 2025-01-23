package com.example.wafflestudio_toyproject.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.databinding.ItemAddButtonBinding
import com.example.wafflestudio_toyproject.databinding.ItemSelectedImageBinding

class SelectedImagesAdapter(
    private val items: MutableList<Uri>,
    private val onRemoveImage: (Uri) -> Unit,
    private val onAddImageClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_IMAGE = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    inner class ImageViewHolder(private val binding: ItemSelectedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            Glide.with(binding.imageView.context)
                .load(uri)
                .placeholder(R.drawable.post_image)
                .into(binding.imageView)
            
            /* 이미지 삭제 버튼
            binding.removeButton.setOnClickListener {
                onRemoveImage(uri)
            }
             */
        }
    }

    inner class AddButtonViewHolder(private val binding: ItemAddButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.addImageButton.setOnClickListener {
                onAddImageClick()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) VIEW_TYPE_IMAGE else VIEW_TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            val binding = ItemSelectedImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ImageViewHolder(binding)
        } else {
            val binding = ItemAddButtonBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            AddButtonViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(items[position])
        } else if (holder is AddButtonViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }
}
