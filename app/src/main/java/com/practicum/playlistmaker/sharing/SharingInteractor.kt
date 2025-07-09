package com.practicum.playlistmaker.sharing

import com.practicum.playlistmaker.sharing.domain.model.EmailData

interface SharingInteractor {
    fun shareApp(link: String)
    fun openTerms(link: String)
    fun openSupport(data: EmailData)
}