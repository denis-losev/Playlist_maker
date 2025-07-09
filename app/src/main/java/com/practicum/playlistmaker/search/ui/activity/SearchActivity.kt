//package com.practicum.playlistmaker.search.ui.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import android.view.inputmethod.EditorInfo
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.isVisible
//import com.google.gson.Gson
//import com.practicum.playlistmaker.R
//import com.practicum.playlistmaker.databinding.ActivitySearchBinding
//import com.practicum.playlistmaker.search.domain.model.Track
//import com.practicum.playlistmaker.search.ui.TrackAdapter
//import com.practicum.playlistmaker.Constants.TRACK
//import com.practicum.playlistmaker.player.ui.activity.PlayerActivity
//import com.practicum.playlistmaker.search.SearchState
//import com.practicum.playlistmaker.search.domain.ClickDebouncer
//import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
//class SearchActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivitySearchBinding
//    private val adapter = TrackAdapter { tapOnTrack(it) }
//
//    private val viewModel: SearchViewModel by viewModel()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySearchBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        setupUI()
//        setupObservers()
//    }
//
//    private fun setupUI() {
//        binding.tracksList.adapter = adapter
//        binding.toolbar.setNavigationOnClickListener { finish() }
//
//        binding.clearText.setOnClickListener {
//            binding.searchBar.text.clear()
//            viewModel.showHistoryIfAvailable()
//        }
//
//        binding.clearHistoryButton.setOnClickListener { viewModel.clearHistory() }
//
//        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) viewModel.showHistoryIfAvailable()
//        }
//
//        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                viewModel.onSearchClicked()
//                true
//            } else false
//        }
//
//        binding.searchBar.addTextChangedListener(searchInputWatcher)
//
//        binding.refresh.setOnClickListener { viewModel.onSearchClicked() }
//    }
//
//    private fun setupObservers() {
//        viewModel.getState().observe(this) { state ->
//            when (state) {
//                SearchState.Init -> changeState(state)
//                is SearchState.EmptyResult -> changeState(state)
//                is SearchState.Error -> changeState(state)
//                is SearchState.History -> changeState(state)
//                SearchState.Loading -> changeState(state)
//                is SearchState.SearchResult -> changeState(state)
//            }
//        }
//    }
//
//    private val searchInputWatcher = object : TextWatcher {
//        override fun beforeTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {}
//
//        override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {
//
//            binding.clearText.isVisible = !input.isNullOrEmpty()
//            viewModel.onSearchQueryChanged(input.toString())
//        }
//
//        override fun afterTextChanged(input: Editable?) {}
//    }
//
//    private fun changeState(state: SearchState) {
//        with(binding) {
//            progressBar.visibility = View.GONE
//            errorContainer.visibility = View.GONE
//            refresh.visibility = View.GONE
//            tracksList.visibility = View.GONE
//            searchHistoryTitle.visibility = View.GONE
//            clearHistoryButton.visibility = View.GONE
//
//            when (state) {
//                SearchState.Init -> return
//                is SearchState.EmptyResult -> {
//                    errorContainer.visibility = View.VISIBLE
//                    errorEmoji.setImageResource(R.drawable.error404emoji)
//                    errorMessage.text = state.message.resolve(this@SearchActivity)
//                }
//                is SearchState.History -> {
//                    adapter.tracks = state.tracks as ArrayList<Track>
//                    adapter.notifyDataSetChanged()
//                    tracksList.visibility = View.VISIBLE
//                    searchHistoryTitle.visibility = View.VISIBLE
//                    clearHistoryButton.visibility = View.VISIBLE
//                }
//                SearchState.Loading -> progressBar.visibility = View.VISIBLE
//                is SearchState.SearchResult -> {
//                    adapter.tracks = state.tracks as ArrayList<Track>
//                    adapter.notifyDataSetChanged()
//
//                    tracksList.visibility = View.VISIBLE
//                }
//                is SearchState.Error -> {
//                    errorContainer.visibility = View.VISIBLE
//                    refresh.visibility = View.VISIBLE
//                    errorEmoji.setImageResource(R.drawable.interneterroremoji)
//                    errorMessage.text = state.errorMessage.resolve(this@SearchActivity)
//                }
//            }
//        }
//    }
//
//    private fun tapOnTrack(track: Track) {
//        ClickDebouncer().tryClick {
//            viewModel.addTrackToHistory(track)
//            val intent = Intent(this, PlayerActivity::class.java).apply {
//                putExtra(TRACK, Gson().toJson(track))
//            }
//            startActivity(intent)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        binding.searchBar.removeTextChangedListener(searchInputWatcher)
//    }
//}