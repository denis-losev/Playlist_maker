package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.track.Track
import com.practicum.playlistmaker.track.TrackAdapter

class SearchActivity : AppCompatActivity() {

    private var searchValue: String = EMPTY_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        setClearTextButton()
        setRecyclerView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_VALUE, searchValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val searchBarInput = findViewById<EditText>(R.id.search_bar)
        searchBarInput.setText(savedInstanceState.getString(SEARCH_VALUE))
    }

    private fun setClearTextButton() {
        val searchBarInput = findViewById<EditText>(R.id.search_bar)
        val clearFieldButton = findViewById<ImageView>(R.id.clear_text)

        clearFieldButton.isVisible = false

        searchBarInput.setOnFocusChangeListener { view, b ->
            if (b.not()) {
                val inputService =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputService.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        searchBarInput.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                clearFieldButton.isVisible = false
            } else {
                clearFieldButton.isVisible = true
                searchValue = text.toString()
            }
        }

        clearFieldButton.setOnClickListener {
            searchBarInput.setText("")
            searchBarInput.clearFocus()
        }
    }

    private fun setRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.tracks_list)
        val adapter = TrackAdapter(tracks)
        recyclerView.adapter = adapter
    }

    companion object {
        const val EMPTY_STRING = ""
        const val SEARCH_VALUE = "SEARCH_VALUE"

        val tracks = listOf(
            Track(
                "Smells Like Teen Spirit",
                "Nirvana",
                "5:01",
                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Billie Jean",
                "Michael Jackson",
                "4:35",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                "Stayin' Alive",
                "Bee Gees",
                "4:10",
                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Whole Lotta Love",
                "Led Zeppelin",
                "5:33",
                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                "Sweet Child O'Mine",
                "Guns N' Roses",
                "5:03",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )
        )
    }
}