package com.practicum.playlistmaker.sharing.domain.impl

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.sharing.SharingInteractor
import com.practicum.playlistmaker.sharing.domain.ExternalNavigator
import com.practicum.playlistmaker.sharing.domain.model.EmailData

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val context: Context
) : SharingInteractor {
    override fun shareApp() {
        externalNavigator.shareLink(getShareAppLink())
    }

    override fun openTerms() {
        externalNavigator.openLink(getTermsLink())
    }

    override fun openSupport() {
        externalNavigator.openEmail(getSupportEmailData())
    }

    private fun getShareAppLink(): String {
        return context.getString(R.string.android_dev_link)
    }

    private fun getSupportEmailData(): EmailData {
        return EmailData(
            arrayOf(context.getString(R.string.my_email)),
            context.getString(R.string.support_mail_theme),
            context.getString(R.string.support_mail_body_content)
        )
    }

    private fun getTermsLink(): String {
        return context.getString(R.string.user_agreement_link)
    }
}