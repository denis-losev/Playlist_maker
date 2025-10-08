package com.practicum.playlistmaker.main.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_main_container) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        val destinationsWithoutBottomBar = setOf(
            R.id.playerFragment,
            R.id.fragment_create_playlist,
            R.id.playlistFragment,
            R.id.editPlaylistFragment
        )

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            binding.bottomNavigationView.isVisible = destination.id !in destinationsWithoutBottomBar
        }
    }
}