package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.search_button)
        val mediaLibraryButton = findViewById<Button>(R.id.media_button)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        searchButton.setOnClickListener{
            val displayIntent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(displayIntent)
        }

        mediaLibraryButton.setOnClickListener{
            val displayIntent = Intent(this@MainActivity, MediaActivity::class.java)
            startActivity(displayIntent)
        }

        settingsButton.setOnClickListener{
            val displayIntent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
    }
}