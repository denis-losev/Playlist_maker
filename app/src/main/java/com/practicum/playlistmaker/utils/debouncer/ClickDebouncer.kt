package com.practicum.playlistmaker.utils.debouncer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClickDebouncer(
    private val debounceDelay: Long = 1000L,
    private val coroutineScope: CoroutineScope
) {

    private var isClickAllowed = true

    fun tryClick(onClick: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            onClick()
            coroutineScope.launch {
                delay(debounceDelay)
                isClickAllowed = true
            }
        }
    }
}