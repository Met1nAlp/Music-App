package com.example.retrofitmusic

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ListRecyclerviewBinding


class ListAdapter(var list: List<Veriler>) : RecyclerView.Adapter<ListAdapter.View_Holder>()
{
    private lateinit var postService: DeezerApiService


    class View_Holder( val binding: ListRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_Holder
    {
        val binding = ListRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        postService = ApiClient.getClient().create(DeezerApiService::class.java)

        return View_Holder(binding)
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    override fun onBindViewHolder(holder: View_Holder, position: Int)
    {
        val secilen = list[position]

        with(holder.binding)
        {
            SarkiismiTextView.text = secilen.title
            artistDurationTextView.text = "${secilen.artist.name} • ${secilen.duration / 60}:${secilen.duration % 60}"

            Glide.with(holder.itemView.context)
                .load(secilen.album.cover_medium)
                .into(holder.binding.resimImageView)

/*
            favoriteIcon.setOnClickListener {

                secilen.isFavorite = !secilen.isFavorite

                if (secilen.isFavorite)
                {
                    holder.binding.favoriteIcon.setColorFilter(
                        ContextCompat.getColor(favoriteIcon.context , R.color.favorite_color))
                }
                else
                {
                    holder.binding.favoriteIcon.setColorFilter(
                        ContextCompat.getColor(favoriteIcon.context , R.color.black))
                }

            }

 */
            root.setOnClickListener {
                val intent = Intent(root.context, MainActivity::class.java)
                intent.putExtra("song_list", ArrayList(list))
                intent.putExtra("song_index", position)
                root.context.startActivity(intent)
            }
        }
    }

    fun updateList(newList: List<Veriler>)
    {
        list = newList
        notifyDataSetChanged()
    }



}