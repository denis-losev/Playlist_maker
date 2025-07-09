package com.practicum.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.activity.PlayerActivity
import com.practicum.playlistmaker.search.SearchState
import com.practicum.playlistmaker.search.domain.ClickDebouncer
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
import com.practicum.playlistmaker.utils.BindingFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BindingFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModel()
    private val clickDebouncer = ClickDebouncer()

    private val adapter = TrackAdapter { tapOnTrack(it) }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.tracksList.adapter = adapter

        binding.clearText.setOnClickListener {
            binding.searchBar.text.clear()
            viewModel.showHistoryIfAvailable()
        }

        binding.clearHistoryButton.setOnClickListener { viewModel.clearHistory() }

        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.showHistoryIfAvailable()
        }

        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchClicked()
                true
            } else false
        }

        binding.searchBar.addTextChangedListener(searchInputWatcher)

        binding.refresh.setOnClickListener { viewModel.onSearchClicked() }
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                SearchState.Init -> changeState(state)
                is SearchState.EmptyResult -> changeState(state)
                is SearchState.Error -> changeState(state)
                is SearchState.History -> changeState(state)
                SearchState.Loading -> changeState(state)
                is SearchState.SearchResult -> changeState(state)
            }
        }
    }

    private val searchInputWatcher = object : TextWatcher {
        override fun beforeTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {

            binding.clearText.isVisible = !input.isNullOrEmpty()
            viewModel.onSearchQueryChanged(input.toString())
        }

        override fun afterTextChanged(input: Editable?) {}
    }

    private fun changeState(state: SearchState) {
        with(binding) {
            progressBar.visibility = View.GONE
            errorContainer.visibility = View.GONE
            refresh.visibility = View.GONE
            tracksList.visibility = View.GONE
            searchHistoryTitle.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE

            when (state) {
                SearchState.Init -> return
                is SearchState.EmptyResult -> {
                    errorContainer.visibility = View.VISIBLE
                    errorEmoji.setImageResource(R.drawable.error404emoji)
                    errorMessage.text = state.message.resolve(requireContext())
                }
                is SearchState.History -> {
                    adapter.tracks = ArrayList(state.tracks)
                    adapter.notifyDataSetChanged()
                    tracksList.visibility = View.VISIBLE
                    searchHistoryTitle.visibility = View.VISIBLE
                    clearHistoryButton.visibility = View.VISIBLE
                }
                SearchState.Loading -> progressBar.visibility = View.VISIBLE
                is SearchState.SearchResult -> {
                    adapter.tracks = state.tracks as ArrayList<Track>
                    adapter.notifyDataSetChanged()

                    tracksList.visibility = View.VISIBLE
                }
                is SearchState.Error -> {
                    errorContainer.visibility = View.VISIBLE
                    refresh.visibility = View.VISIBLE
                    errorEmoji.setImageResource(R.drawable.interneterroremoji)
                    errorMessage.text = state.errorMessage.resolve(requireContext())
                }
            }
        }
    }

    private fun tapOnTrack(track: Track) {
        clickDebouncer.tryClick {
            viewModel.addTrackToHistory(track)
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(TRACK, Gson().toJson(track))
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        binding.searchBar.removeTextChangedListener(searchInputWatcher)
        super.onDestroyView()
    }
}