package com.practicum.playlistmaker.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.practicum.playlistmaker.sharing.domain.model.EmailData
import com.practicum.playlistmaker.utils.BindingFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModel{
        parametersOf(requireActivity())
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            binding.themeSwitcher.isChecked = state.isDarkMode
            AppCompatDelegate.setDefaultNightMode(
                if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        val activity = requireActivity()

        binding.shareButton.setOnClickListener {
            viewModel.shareApp(activity.getString(R.string.android_dev_link))
        }

        binding.supportButton.setOnClickListener {
            viewModel.contactSupport(EmailData(
                arrayOf(activity.getString(R.string.my_email)),
                activity.getString(R.string.support_mail_theme),
                activity.getString(R.string.support_mail_body_content)
            ))
        }

        binding.agreementButton.setOnClickListener {
            viewModel.openTerms(activity.getString(R.string.user_agreement_link))
        }
    }
}