package com.example.retrofitmusic

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.IzgaraOgeAlbumBinding

class AnaSayfaAlbumAdapter (val list: List<Playlist>) : RecyclerView.Adapter<AnaSayfaAlbumAdapter.AlbumHolder>()
{
    class AlbumHolder( val binding: IzgaraOgeAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): AlbumHolder
    {
        val binding = IzgaraOgeAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumHolder, position: Int)
    {
        val secilenPlaylist = list[position]

        with(holder.binding)
        {
            albumAdiTextView.text = secilenPlaylist.title

            Glide.with(holder.itemView.context)
                .load(secilenPlaylist.picture_medium)
                .into(holder.binding.albumKapakImageView)

            root.setOnClickListener {
                val intent = Intent(root.context, SarkiListe::class.java)
                intent.putExtra("playlist_id", secilenPlaylist.id)
                root.context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int
    {
        return list.size
    }
}