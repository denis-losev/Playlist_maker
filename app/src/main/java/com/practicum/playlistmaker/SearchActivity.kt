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

    companion object {
        const val EMPTY_STRING = ""
        const val SEARCH_VALUE = "SEARCH_VALUE"
    }
}