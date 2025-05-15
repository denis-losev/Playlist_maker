package com.practicum.playlistmaker.presentation.activities.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.TrackAdapter
import com.practicum.playlistmaker.presentation.activities.player.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }
    private lateinit var progressBar: ProgressBar
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
    private lateinit var searchHistory: SearchHistoryRepository
    private lateinit var historyHeader: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var playerActivity: Intent

    private val tracksInteractor = Creator.provideTracksInteractor()

    private val adapter = TrackAdapter { tapOnTrack(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        Creator.init(this)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.tracks_list)
        searchBarInput = findViewById(R.id.search_bar)
        clearFieldButton = findViewById(R.id.clear_text)
        errorContainer = findViewById(R.id.error_container)
        errorEmoji = findViewById(R.id.error_emoji)
        errorMessage = findViewById(R.id.error_message)
        refreshButton = findViewById(R.id.refresh)

        searchHistory = Creator.provideSearchHistoryRepository()
        historyHeader = findViewById(R.id.search_history_title)
        clearHistoryButton = findViewById(R.id.clear_history_button)

        progressBar = findViewById(R.id.progressBar)

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
            if (searchHistory.getSearchHistory().isNotEmpty() && hasFocus) {
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

            if (searchHistory.getSearchHistory().isNotEmpty()) {
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
        if (searchBarInput.text.isNotEmpty()) {


            changeState(SearchActivityState.LOADING)

            tracksInteractor.searchTracks(
                searchBarInput.text.toString(),
                object : TracksInteractor.TracksConsumer {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun consume(recievedTracks: List<Track>) {
                        runOnUiThread {
                            if (recievedTracks.isNotEmpty()) {
                                tracks.clear()
                                tracks.addAll(recievedTracks)
                                adapter.notifyDataSetChanged()
                                changeState(SearchActivityState.SEARCH)
                            } else if (recievedTracks.isEmpty()) {
                                changeState(SearchActivityState.NOT_FOUND)
                            }
                        }
                    }

                    override fun consumeError(error: String?) {
                        runOnUiThread {
                            changeState(SearchActivityState.NO_INTERNET_CONNECTION)
                        }
                    }
                })
        }
    }

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {

            if (!input.isNullOrEmpty()) {
                searchDebounce()
            }

            clearFieldButton.isVisible = !input.isNullOrEmpty()

            if (searchBarInput.hasFocus()
                && input.isNullOrEmpty()
                && searchHistory.getSearchHistory().isNotEmpty()
            ) {
                changeState(SearchActivityState.HISTORY)
            }
        }

        override fun afterTextChanged(input: Editable?) {}

    }

    private fun tapOnTrack(track: Track) {
        if (clickDebounce()) {
            searchHistory.addTrackHistory(track)
            playerActivity = Intent(this, PlayerActivity::class.java)
            playerActivity.putExtra(TRACK, Gson().toJson(track))
            startActivity(playerActivity)
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun changeState(state: SearchActivityState) {
        when (state) {
            SearchActivityState.SEARCH -> {
                progressBar.visibility = View.GONE
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                adapter.tracks = tracks
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE
                historyHeader.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            SearchActivityState.NOT_FOUND -> {
                progressBar.visibility = View.GONE
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
                progressBar.visibility = View.GONE
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
                progressBar.visibility = View.GONE
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                adapter.tracks = searchHistory.getSearchHistory()
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE
                historyHeader.visibility = View.VISIBLE
                clearHistoryButton.visibility = View.VISIBLE
            }

            SearchActivityState.LOADING -> {
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                errorContainer.visibility = View.GONE
                refreshButton.visibility = View.GONE
                historyHeader.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }
        }
    }

    companion object {
        const val EMPTY_STRING = ""
        const val SEARCH_VALUE = "SEARCH_VALUE"
        const val TRACK = "TRACK"
        const val TRACKS_LIST = "TRACKS_LIST"
        const val IS_RECYCLER_VISIBLE = "IS_RECYCLER_VISIBLE"
        const val IS_ERROR_VISIBLE = "IS_ERROR_VISIBLE"
        const val IS_REFRESH_VISIBLE = "IS_REFRESH_VISIBLE"
        const val IS_HISTORY_VISIBLE = "IS_HISTORY_VISIBLE"
        const val ERROR_TYPE = "ERROR_TYPE"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}