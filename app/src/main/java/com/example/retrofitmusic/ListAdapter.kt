package com.example.retrofitmusic

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ListRecyclerviewBinding

class ListAdapter(val list: List<Veriler>) : RecyclerView.Adapter<ListAdapter.View_Holder>()
{

    class View_Holder( val binding: ListRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_Holder
    {
        val binding = ListRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return View_Holder(binding)
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    override fun onBindViewHolder(holder: View_Holder, position: Int)
    {
        val secilen = list[position]

        holder.binding.SarkiismiTextView.text = secilen.title

        Glide.with(holder.binding.root.context)
            .load(secilen.cover_medium)
            .placeholder(R.drawable.ileri_asset)
            .error(R.drawable.devam_asset)
            .into(holder.binding.resimImageView)

        holder.itemView.setOnClickListener {

            val intent = Intent(holder.itemView.context, MainActivity::class.java)

            intent.putExtra("id", secilen.id)
            println(secilen.id)
            holder.itemView.context.startActivity(intent)


        }
    }


}