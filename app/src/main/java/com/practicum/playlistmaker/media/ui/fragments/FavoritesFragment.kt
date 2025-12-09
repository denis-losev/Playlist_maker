package com.practicum.playlistmaker.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.composable.screens.FavoritesScreen
import com.practicum.playlistmaker.media.ui.view_model.favorites.FavoritesViewModel
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FavoritesScreen(
                    onTrackClick = { track ->
                        clickDebouncer.tryClick {
                            viewModel.addTrackToHistory(track)
                            val bundle = Bundle().apply { putParcelable(TRACK, track) }
                            findNavController().navigate(
                                R.id.playerFragment,
                                bundle
                            )
                        }
                    },
                    viewModel = viewModel
                )
            }
        }
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}