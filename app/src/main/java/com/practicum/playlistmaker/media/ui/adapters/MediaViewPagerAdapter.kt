package com.practicum.playlistmaker.media.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.practicum.playlistmaker.media.ui.fragments.FavoritesFragment
import com.practicum.playlistmaker.media.ui.fragments.PlaylistsFragment

class MediaViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentsList: List<Fragment> =
        listOf(FavoritesFragment.Companion.newInstance(), PlaylistsFragment.Companion.newInstance())

    override fun getItemCount(): Int {
        return fragmentsList.count()
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }
}