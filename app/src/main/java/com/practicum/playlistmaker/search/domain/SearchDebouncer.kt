package com.practicum.playlistmaker.search.domain

import android.os.Handler
import android.os.Looper

class SearchDebouncer(
    private val delayMillis: Long = 2000L,
    private val handler: Handler = Handler(Looper.getMainLooper())
) {
    private var searchRunnable: Runnable? = null

    fun submit(action: () -> Unit) {
        searchRunnable?.let { handler.removeCallbacks(it) }
        searchRunnable = Runnable { action() }
        handler.postDelayed(searchRunnable!!, delayMillis)
    }

    fun cancel() {
        searchRunnable?.let { handler.removeCallbacks(it) }
    }
}