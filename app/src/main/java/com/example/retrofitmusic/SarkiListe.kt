package com.example.retrofitmusic

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    private fun setupAdapter()
    {
        println("ayse1")
        binding.recyclerView.apply {
            println("ayse2")
            layoutManager = LinearLayoutManager(this@SarkiListe)
            adapter = this@SarkiListe.adapter
        }
    }

    private fun setData()
    {
        println("ali1")
        val call = postService.listPost("302127")

        call.enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>)
            {
                println("ali2")
                if (response.isSuccessful)
                {
                    println("ali3")
                    response.body()?.data?.let { tracks ->
                        postList.clear()
                        postList.addAll(tracks)
                        adapter.notifyDataSetChanged()

                        println("ali4")
                    } ?: run {
                        Toast.makeText(this@SarkiListe, "Veri alınamadı", Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    println("lkadf")
                    Toast.makeText(this@SarkiListe, "Hata: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                Toast.makeText(this@SarkiListe, "Ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}