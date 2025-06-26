package com.example.retrofitmusic

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ListeOgeSarkiBinding

class AnaSayfaSarkiAdapter( val list: List<Veriler>) : RecyclerView.Adapter<AnaSayfaSarkiAdapter.ViewHolder>()
{
    class ViewHolder( val binding: ListeOgeSarkiBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }


    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolder
    {
        val binding = ListeOgeSarkiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val secilen = list[position]

        with(holder.binding)
        {
            sarkiAdiTextView.text = secilen.title
            sarkiDetayTextView.text = "${secilen.artist.name} • ${secilen.duration / 60}:${secilen.duration % 60}"

            Glide.with(holder.itemView.context)
                .load(secilen.album.cover_medium)
                .into(holder.binding.sarkiKapakImageView)


            root.setOnClickListener {
                val intent = Intent(root.context, MainActivity::class.java)
                intent.putExtra("song_list", ArrayList(list))
                intent.putExtra("song_index", position)
                root.context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int
    {
        return list.size
    }
}