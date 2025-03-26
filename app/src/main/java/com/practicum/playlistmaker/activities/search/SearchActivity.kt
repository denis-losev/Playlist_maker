package com.practicum.playlistmaker.activities.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.App
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
    private var currentErrorType: SearchActivityState? = null
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBarInput: EditText
    private lateinit var clearFieldButton: ImageView
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorEmoji: ImageView
    private lateinit var errorMessage: TextView
    private lateinit var refreshButton: Button
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyHeader: TextView
    private lateinit var clearHistoryButton: Button

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(ITunesApi::class.java)

    private val adapter = TrackAdapter { tapOnTrack(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.tracks_list)
        searchBarInput = findViewById(R.id.search_bar)
        clearFieldButton = findViewById(R.id.clear_text)
        errorContainer = findViewById(R.id.error_container)
        errorEmoji = findViewById(R.id.error_emoji)
        errorMessage = findViewById(R.id.error_message)
        refreshButton = findViewById(R.id.refresh)

        searchHistory = SearchHistory((applicationContext as App).sharedPrefs)
        historyHeader = findViewById(R.id.search_history_title)
        clearHistoryButton = findViewById(R.id.clear_history_button)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView.adapter = adapter

        refreshButton.setOnClickListener {
            searchRequest()
        }

        searchButtonListener()
        clearFieldButtonListener()

        searchBarInput.setOnFocusChangeListener { _, hasFocus ->
            if (searchHistory.getTracksHistory().isNotEmpty() && hasFocus) {
                changeState(SearchActivityState.HISTORY)
            }
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            changeState(SearchActivityState.SEARCH)
            recyclerView.visibility = View.GONE
        }

        searchBarInput.addTextChangedListener(inputWatcher)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_VALUE, searchValue)
        outState.putBoolean(IS_HISTORY_VISIBLE, historyHeader.isVisible)
        outState.putParcelableArrayList(TRACKS_LIST, ArrayList(tracks))
        outState.putBoolean(IS_RECYCLER_VISIBLE, recyclerView.isVisible)
        outState.putBoolean(IS_ERROR_VISIBLE, errorContainer.isVisible)
        outState.putBoolean(IS_REFRESH_VISIBLE, refreshButton.isVisible)
        outState.putString(ERROR_TYPE, currentErrorType?.name)
        searchBarInput.addTextChangedListener(inputWatcher)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchBarInput.setText(savedInstanceState.getString(SEARCH_VALUE))

        val savedTracks: ArrayList<Track>? =
            BundleCompat.getParcelableArrayList(savedInstanceState, TRACKS_LIST, Track::class.java)
        if (!savedTracks.isNullOrEmpty()) {
            tracks.clear()
            tracks.addAll(savedTracks)
            adapter.tracks = tracks
            adapter.notifyDataSetChanged()
        }

        recyclerView.isVisible = savedInstanceState.getBoolean(IS_RECYCLER_VISIBLE, false)
        errorContainer.isVisible = savedInstanceState.getBoolean(IS_ERROR_VISIBLE, false)
        refreshButton.isVisible = savedInstanceState.getBoolean(IS_REFRESH_VISIBLE, false)
        historyHeader.isVisible = savedInstanceState.getBoolean(IS_HISTORY_VISIBLE, false)
        clearHistoryButton.isVisible = savedInstanceState.getBoolean(IS_HISTORY_VISIBLE, false)

        savedInstanceState.getString(ERROR_TYPE)?.let { type ->
            currentErrorType = SearchActivityState.valueOf(type)
            changeState(currentErrorType!!)
        }
    }

    private fun clearFieldButtonListener() {
        clearFieldButton.setOnClickListener {
            searchBarInput.text.clear()

            val inputService =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputService.hideSoftInputFromWindow(searchBarInput.windowToken, 0)

            if (searchHistory.getTracksHistory().isNotEmpty()) {
                changeState(SearchActivityState.HISTORY)
            } else {
                changeState(SearchActivityState.SEARCH)
            }
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
                            changeState(SearchActivityState.SEARCH)
                        } else if (response.body()?.results?.isEmpty() == true) {
                            changeState(SearchActivityState.NOT_FOUND)
                        }
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, response: Throwable) {
                    changeState(SearchActivityState.NO_INTERNET_CONNECTION)
                }
            })
    }

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {
            clearFieldButton.isVisible = !input.isNullOrEmpty()

            if (searchBarInput.hasFocus()
                && input.isNullOrEmpty()
                && searchHistory.getTracksHistory().isNotEmpty()
            ) {
                changeState(SearchActivityState.HISTORY)
            }
        }

        override fun afterTextChanged(input: Editable?) {}

    }

    private fun tapOnTrack(track: Track) {
        searchHistory.addTrackToHistory(track)
    }

    private fun changeState(state: SearchActivityState) {
        when (state) {
            SearchActivityState.SEARCH -> {
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                adapter.tracks = tracks
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE
                historyHeader.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchActivityState.NOT_FOUND -> {
                currentErrorType = SearchActivityState.NOT_FOUND
                recyclerView.visibility = View.GONE
                refreshButton.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                errorEmoji.setImageResource(R.drawable.error404emoji)
                errorMessage.setText(R.string.not_found_error_text)
                historyHeader.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchActivityState.NO_INTERNET_CONNECTION -> {
                currentErrorType = SearchActivityState.NO_INTERNET_CONNECTION
                recyclerView.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                errorEmoji.setImageResource(R.drawable.interneterroremoji)
                errorMessage.setText(R.string.internet_error_text)
                refreshButton.visibility = View.VISIBLE
                historyHeader.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchActivityState.HISTORY -> {
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                adapter.tracks = searchHistory.getTracksHistory()
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE
                historyHeader.visibility = View.VISIBLE
                clearHistoryButton.visibility = View.VISIBLE
            }
        }
    }

    private companion object {
        const val EMPTY_STRING = ""
        const val SEARCH_VALUE = "SEARCH_VALUE"
        const val TRACKS_LIST = "TRACKS_LIST"
        const val IS_RECYCLER_VISIBLE = "IS_RECYCLER_VISIBLE"
        const val IS_ERROR_VISIBLE = "IS_ERROR_VISIBLE"
        const val IS_REFRESH_VISIBLE = "IS_REFRESH_VISIBLE"
        const val IS_HISTORY_VISIBLE = "IS_HISTORY_VISIBLE"
        const val ERROR_TYPE = "ERROR_TYPE"
        const val BASE_URL = "https://itunes.apple.com"
    }
}