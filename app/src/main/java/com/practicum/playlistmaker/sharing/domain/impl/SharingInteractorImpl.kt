package com.practicum.playlistmaker.sharing.domain.impl

import com.practicum.playlistmaker.sharing.SharingInteractor
import com.practicum.playlistmaker.sharing.domain.ExternalNavigator
import com.practicum.playlistmaker.sharing.domain.model.EmailData

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator
) : SharingInteractor {

    override fun shareApp(link: String) {
        externalNavigator.shareLink(link)
    }

    override fun openTerms(link: String) {
        externalNavigator.openLink(link)
    }

    override fun openSupport(data: EmailData) {
        externalNavigator.openEmail(data)
    }
}