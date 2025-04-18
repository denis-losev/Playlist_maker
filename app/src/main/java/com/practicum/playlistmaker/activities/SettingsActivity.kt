package com.practicum.playlistmaker.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val themeSwitcher = findViewById<Switch>(R.id.themeSwitcher)
        val shareApp = findViewById<TextView>(R.id.share_button)
        val supportButton = findViewById<TextView>(R.id.support_button)
        val userAgreement = findViewById<TextView>(R.id.agreement_button)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
        }
        themeSwitcher.isChecked = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)

        shareApp.setOnClickListener {
            val shareLink = getText(R.string.android_dev_link)
            val shareInt = Intent(Intent.ACTION_SEND)
            shareInt.type = "text/plain"
            shareInt.putExtra(Intent.EXTRA_TEXT, shareLink)
            startActivity(shareInt)
        }

        supportButton.setOnClickListener {
            val email = getText(R.string.my_email)
            val theme = getText(R.string.support_mail_theme)
            val message = getText(R.string.support_mail_body_content)
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                .putExtra(Intent.EXTRA_SUBJECT, theme)
                .putExtra(Intent.EXTRA_TEXT, message)
            startActivity(shareIntent)
        }

        userAgreement.setOnClickListener {
            val agreementLink = getString(R.string.user_agreement_link)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementLink))
            startActivity(intent)
        }
    }
}