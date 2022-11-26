package com.example.newsscrap.newslist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsscrap.data.News
import com.example.newsscrap.data.NewsApiService
import com.example.newsscrap.data.NewsApplication
import com.example.newsscrap.databinding.FragmentNewsListBinding
import com.example.newsscrap.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private lateinit var binding: FragmentNewsListBinding
private lateinit var newsAdapter: NewsListAdapter
private lateinit var linearLayoutManager: RecyclerView.LayoutManager
private var news : List<News?> = listOf<News>()

class FragmentNewsList : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        //TODO: Corregir queries
        getNews("news")
        getNews("news1")
        getNews("news2")
    }


    //TODO corregir con la url de la api original
    @SuppressLint("NotifyDataSetChanged")
    private fun getNews(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(NewsApiService::class.java)
                .getNewsList("news/$query")
            val newsResponse = call.body()
            activity?.runOnUiThread {
                if (call.isSuccessful) {
                    news = news + newsResponse!!
                    news = news.shuffled()
                    newsAdapter.notifyDataSetChanged()
                } else {
                    Log.i("ERROR", "NO HA FUNCIONADO")
                }
            }
        }
    }


    private fun initRecyclerView() {
        //Inicializamos el adapter
        newsAdapter = NewsListAdapter(news, ::listener, ::guardarNews)

        //Organizando la vista de la activity
        linearLayoutManager = LinearLayoutManager(activity)

        //Metemos adapter y layout dentro del RV
        binding.rvNewsList.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = newsAdapter
        }
    }

    private fun guardarNews(news: News?){  //TODO: Guardar en BBDD room
        Thread{
            NewsApplication.database.NewsDao().addNew(news)
        }.start()
    }


    private fun listener(url: String?){
        findNavController()
            .navigate(FragmentNewsListDirections.actionNewsListToNewsDetails
                (url))
    }


    //TODO corregir con la llamada original
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://634694e3745bd0dbd3811262.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}

