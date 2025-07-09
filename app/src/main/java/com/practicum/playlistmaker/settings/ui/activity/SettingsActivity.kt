//package com.practicum.playlistmaker.settings.ui.activity
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import com.practicum.playlistmaker.databinding.ActivitySettingsBinding
//import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//import org.koin.core.parameter.parametersOf
//
//class SettingsActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivitySettingsBinding
//
//    private val viewModel: SettingsViewModel by viewModel {
//        parametersOf(this@SettingsActivity)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySettingsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.toolbar.setNavigationOnClickListener { finish() }
//
//        viewModel.getState().observe(this) { state ->
//            binding.themeSwitcher.isChecked = state.isDarkMode
//            AppCompatDelegate.setDefaultNightMode(
//                if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
//                else AppCompatDelegate.MODE_NIGHT_NO
//            )
//        }
//
//        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
//            viewModel.switchTheme(checked)
//        }
//
//        binding.shareButton.setOnClickListener {
//            viewModel.shareApp()
//        }
//
//        binding.supportButton.setOnClickListener {
//            viewModel.contactSupport()
//        }
//
//        binding.agreementButton.setOnClickListener {
//            viewModel.openTerms()
//        }
//    }
//}