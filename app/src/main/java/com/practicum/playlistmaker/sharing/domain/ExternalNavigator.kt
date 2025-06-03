package com.practicum.playlistmaker.sharing.domain

import com.practicum.playlistmaker.sharing.domain.model.EmailData

interface ExternalNavigator {
    fun shareLink(link: String)
    fun openLink(link: String)
    fun openEmail(emailData: EmailData)
}