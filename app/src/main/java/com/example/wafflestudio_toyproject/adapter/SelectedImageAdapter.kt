package com.example.wafflestudio_toyproject.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.databinding.ItemSelectedImageBinding

class SelectedImagesAdapter(
    private val items: MutableList<Uri>,
    private val onRemoveImage: (Uri) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemSelectedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            Glide.with(binding.imageView.context)
                .load(uri)
                .placeholder(R.drawable.post_image)
                .into(binding.imageView)

            binding.removeButton.setOnClickListener {
                onRemoveImage(uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
