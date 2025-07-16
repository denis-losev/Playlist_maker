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
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.activity.PlayerActivity
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
            render(state.toUiState(requireContext()))
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

    private fun render(uiState: SearchUiState) = with(binding) {
        hideAll()

        when (uiState) {
            SearchUiState.Init -> Unit
            SearchUiState.Loading -> progressBar.isVisible = true
            is SearchUiState.ShowError -> showError(uiState)
            is SearchUiState.ShowHistory -> showHistory(uiState.tracks)
            is SearchUiState.ShowTracks -> showSearchResult(uiState.tracks)
        }
    }

    private fun hideAll() {
        with(binding) {
            progressBar.isVisible = false
            errorContainer.isVisible = false
            refresh.isVisible = false
            tracksList.isVisible = false
            searchHistoryTitle.isVisible = false
            clearHistoryButton.isVisible = false
        }
    }

    private fun showError(uiState: SearchUiState.ShowError) = with(binding) {
        errorContainer.isVisible = true
        refresh.isVisible = uiState.showRefresh
        errorEmoji.setImageResource(uiState.emojiRes)
        errorMessage.text = uiState.message
    }

    private fun showHistory(tracks: List<Track>) = with(binding) {
        adapter.tracks = ArrayList(tracks)
        adapter.notifyDataSetChanged()
        tracksList.isVisible = true
        searchHistoryTitle.isVisible = true
        clearHistoryButton.isVisible = true
    }

    private fun showSearchResult(tracks: List<Track>) = with(binding) {
        adapter.tracks = ArrayList(tracks)
        adapter.notifyDataSetChanged()
        tracksList.isVisible = true
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