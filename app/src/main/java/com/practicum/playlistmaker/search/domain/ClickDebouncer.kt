package com.practicum.playlistmaker.search.domain

import android.os.Handler
import android.os.Looper

class ClickDebouncer(private val debounceDelay: Long = 1000L) {

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())

    fun tryClick(onClick: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            onClick()
            handler.postDelayed({ isClickAllowed = true }, debounceDelay)
        }
    }
}