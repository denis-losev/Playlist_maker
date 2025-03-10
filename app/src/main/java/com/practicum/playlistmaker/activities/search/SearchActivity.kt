package com.practicum.playlistmaker.activities.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.iTunesApi.ITunesApi
import com.practicum.playlistmaker.iTunesApi.TrackResponse
import com.practicum.playlistmaker.track.Track
import com.practicum.playlistmaker.track.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SearchActivity : AppCompatActivity() {

    private val tracks = ArrayList<Track>()
    private var searchValue: String = EMPTY_STRING
    private var currentErrorType: SearchResult? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBarInput: EditText
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorEmoji: ImageView
    private lateinit var errorMessage: TextView
    private lateinit var refreshButton: Button

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(ITunesApi::class.java)
    private val adapter = TrackAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.tracks_list)
        searchBarInput = findViewById(R.id.search_bar)
        errorContainer = findViewById(R.id.error_container)
        errorEmoji = findViewById(R.id.error_emoji)
        errorMessage = findViewById(R.id.error_message)
        refreshButton = findViewById(R.id.refresh)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView.adapter = adapter

        refreshButton.setOnClickListener {
            searchRequest()
        }

        setClearTextButton()
        searchButtonListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_VALUE, searchValue)
        outState.putParcelableArrayList(TRACKS_LIST, ArrayList(tracks))
        outState.putBoolean(IS_RECYCLER_VISIBLE, recyclerView.isVisible)
        outState.putBoolean(IS_ERROR_VISIBLE, errorContainer.isVisible)
        outState.putBoolean(IS_REFRESH_VISIBLE, refreshButton.isVisible)
        outState.putString(ERROR_TYPE, currentErrorType?.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchBarInput.setText(savedInstanceState.getString(SEARCH_VALUE))

        val savedTracks: ArrayList<Track>? = BundleCompat.getParcelableArrayList(savedInstanceState, TRACKS_LIST, Track::class.java)
        if (!savedTracks.isNullOrEmpty()) {
            tracks.clear()
            tracks.addAll(savedTracks)
            adapter.tracks = tracks
            adapter.notifyDataSetChanged()
        }

        recyclerView.isVisible = savedInstanceState.getBoolean(IS_RECYCLER_VISIBLE, false)
        errorContainer.isVisible = savedInstanceState.getBoolean(IS_ERROR_VISIBLE, false)
        refreshButton.isVisible = savedInstanceState.getBoolean(IS_REFRESH_VISIBLE, false)

        savedInstanceState.getString(ERROR_TYPE)?.let { type ->
            currentErrorType = SearchResult.valueOf(type)
            showSearchResult(currentErrorType!!)
        }
    }

    private fun searchButtonListener() {

        searchBarInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequest()
                true
            } else {
                false
            }
        }
    }

    private fun searchRequest() {
        iTunesService.search(searchBarInput.text.toString())
            .enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if (response.code() == 200) {
                        tracks.clear()
                        if (response.body()?.results?.isNotEmpty() == true) {
                            tracks.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                            showSearchResult(SearchResult.SUCCESS)
                        } else if (response.body()?.results?.isEmpty() == true) {
                            showSearchResult(SearchResult.NOT_FOUND)
                        }
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, response: Throwable) {
                    showSearchResult(SearchResult.NO_INTERNET_CONNECTION)
                }

            })
    }

    private fun setClearTextButton() {
        val clearFieldButton = findViewById<ImageView>(R.id.clear_text)

        clearFieldButton.isVisible = false

        searchBarInput.setOnFocusChangeListener { view, b ->
            if (b.not()) {
                val inputService =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputService.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        searchBarInput.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                clearFieldButton.isVisible = false
            } else {
                clearFieldButton.isVisible = true
                searchValue = text.toString()
            }
        }

        clearFieldButton.setOnClickListener {
            recyclerView.visibility = View.GONE
            searchBarInput.setText("")
            searchBarInput.clearFocus()
        }
    }

    private fun showSearchResult(state: SearchResult) {
        when (state) {
            SearchResult.SUCCESS -> {
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                adapter.tracks = tracks
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE
            }

            SearchResult.NOT_FOUND -> {
                recyclerView.visibility = View.GONE
                refreshButton.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                errorEmoji.setImageResource(R.drawable.error404emoji)
                errorMessage.setText(R.string.not_found_error_text)
            }

            SearchResult.NO_INTERNET_CONNECTION -> {
                recyclerView.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                errorEmoji.setImageResource(R.drawable.interneterroremoji)
                errorMessage.setText(R.string.internet_error_text)
                refreshButton.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val EMPTY_STRING = ""
        const val SEARCH_VALUE = "SEARCH_VALUE"
        const val TRACKS_LIST = "TRACKS_LIST"
        const val IS_RECYCLER_VISIBLE = "IS_RECYCLER_VISIBLE"
        const val IS_ERROR_VISIBLE = "IS_ERROR_VISIBLE"
        const val IS_REFRESH_VISIBLE = "IS_REFRESH_VISIBLE"
        const val ERROR_TYPE = "ERROR_TYPE"
        const val BASE_URL = "https://itunes.apple.com"
    }
}