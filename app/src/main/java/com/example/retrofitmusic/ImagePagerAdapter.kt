package com.example.retrofitmusic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ItemImagePagerBinding

class AlbumArtAdapter(private val songList: List<Veriler>) : RecyclerView.Adapter<AlbumArtAdapter.AlbumArtViewHolder>()
{

    class AlbumArtViewHolder(val binding: ItemImagePagerBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumArtViewHolder
    {
        val binding = ItemImagePagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumArtViewHolder(binding)
    }

    override fun getItemCount(): Int
    {
        return songList.size
    }

    override fun onBindViewHolder(holder: AlbumArtViewHolder, position: Int)
    {
        val track = songList[position]
        Glide.with(holder.itemView.context)
            .load(track.album.cover_medium)
            .placeholder(R.drawable.musicapp)
            .error(R.drawable.devam_asset)
            .into(holder.binding.albumArtImageView)
    }
}