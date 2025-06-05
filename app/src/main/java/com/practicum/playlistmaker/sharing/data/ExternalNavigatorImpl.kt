package com.practicum.playlistmaker.sharing.data

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.practicum.playlistmaker.sharing.domain.ExternalNavigator
import com.practicum.playlistmaker.sharing.domain.model.EmailData

class ExternalNavigatorImpl(
    private val activity: Activity
): ExternalNavigator {
    override fun shareLink(link: String) {
        val shareInt = Intent(Intent.ACTION_SEND)
        shareInt.type = "text/plain"
        shareInt.putExtra(Intent.EXTRA_TEXT, link)
        activity.startActivity(shareInt)
    }

    override fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        activity.startActivity(intent)
    }

    override fun openEmail(emailData: EmailData) {
        val shareIntent = Intent(Intent.ACTION_SENDTO)
        shareIntent.data = Uri.parse("mailto:")
        shareIntent
            .putExtra(Intent.EXTRA_EMAIL, emailData.email)
            .putExtra(Intent.EXTRA_SUBJECT, emailData.theme)
            .putExtra(Intent.EXTRA_TEXT, emailData.message)
        activity.startActivity(shareIntent)
    }
}