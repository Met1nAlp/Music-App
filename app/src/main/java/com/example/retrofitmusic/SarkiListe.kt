package com.example.retrofitmusic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.retrofitmusic.ListAdapter
import com.example.retrofitmusic.databinding.ActivitySarkiListeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SarkiListe : AppCompatActivity()
{

    private lateinit var binding: ActivitySarkiListeBinding
    private lateinit var adapter: ListAdapter
    private lateinit var postService: DeezerApiService
    private val postList: MutableList<Veriler> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySarkiListeBinding.inflate(layoutInflater)
        setContentView(binding.root)








        postService = ApiClient.getClient().create(DeezerApiService::class.java)

        // BURADA EKLENMELİ
        adapter = ListAdapter(postList) // postList'i adaptöre geçiriyoruz



        setData()
        setupAdapter()
    }

    private fun setupSearchView()
    {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = postList.filter { it.title.contains(newText ?: "", ignoreCase = true) }
                adapter.updateList(filteredList) // Hata düzeltildi: list -> updateList
                binding.emptyView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                return true
            }
        })
    }

    private fun setData()
    {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        val call = postService.listPost("302127")
        call.enqueue(object : Callback<DeezerResponse> {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>) {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                if (response.isSuccessful) {
                    response.body()?.data?.let { tracks ->
                        postList.clear()
                        postList.addAll(tracks)
                        adapter.notifyDataSetChanged()
                        binding.emptyView.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
                    } ?: run {
                        Toast.makeText(this@SarkiListe, "Veri alınamadı", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@SarkiListe, "Hata: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<DeezerResponse>, t: Throwable) {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                Toast.makeText(this@SarkiListe, "Ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupAdapter()
    {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SarkiListe)
            adapter = this@SarkiListe.adapter
        }
    }
/*
    private fun setData()
    {
        val call = postService.listPost("302127")

        call.enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>)
            {
                if (response.isSuccessful)
                {
                    response.body()?.data?.let { tracks ->
                        postList.clear()
                        postList.addAll(tracks)
                        adapter.notifyDataSetChanged()

                    } ?: run {
                        Toast.makeText(this@SarkiListe, "Veri alınamadı", Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    Toast.makeText(this@SarkiListe, "Hata: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                Toast.makeText(this@SarkiListe, "Ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

 */
}