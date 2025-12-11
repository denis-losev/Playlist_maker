package com.practicum.playlistmaker.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

sealed class UiMessage {
    data class Text(val textMessage: String) : UiMessage()
    data class Resource(@StringRes val resId: Int) : UiMessage()

    fun resolve(context: Context): String {
        return when (this) {
            is Text -> textMessage
            is Resource -> context.getString(resId)
        }
    }

    @Composable
    fun resolve(): String {
        return when (this) {
            is Text -> textMessage
            is Resource -> androidx.compose.ui.res.stringResource(id = resId)
        }
    }
}