package com.practicum.playlistmaker.utils

import android.content.Context
import androidx.annotation.StringRes

sealed class UiMessage {
    data class Text(val textMessage: String) : UiMessage()
    data class Resource(@StringRes val resId: Int) : UiMessage()

    fun resolve(context: Context): String = when (this) {
        is Text -> textMessage
        is Resource -> context.getString(resId)
    }
}