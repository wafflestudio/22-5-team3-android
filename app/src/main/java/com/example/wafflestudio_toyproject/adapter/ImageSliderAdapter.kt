package com.example.wafflestudio_toyproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wafflestudio_toyproject.R
import com.example.wafflestudio_toyproject.databinding.ItemImageSliderBinding

class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Glide.with(binding.imageView.context) // Context를 사용
                .load(imageUrl)
                .placeholder(R.drawable.post_image) // 로드 중 보여줄 이미지
                .error(R.drawable.post_image) // 로드 실패 시 보여줄 이미지
                .into(binding.imageView)
            Log.d("ImageSliderAdapter", "Loaded image: $imageUrl")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}
