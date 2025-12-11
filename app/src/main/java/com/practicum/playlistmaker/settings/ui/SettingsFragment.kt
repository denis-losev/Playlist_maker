package com.practicum.playlistmaker.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.utils.PlaylistMakerTheme
import com.practicum.playlistmaker.settings.ui.composable.SettingsContent
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.practicum.playlistmaker.sharing.domain.model.EmailData
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel {
        parametersOf(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PlaylistMakerTheme {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.getState().observeAsState()
    val context = LocalContext.current

    SettingsContent(
        isDarkMode = state?.isDarkMode ?: false,
        onThemeSwitchChanged = { checked ->
            viewModel.switchTheme(checked)
        },
        onShareClick = {
            viewModel.shareApp(context.getString(R.string.android_dev_link))
        },
        onSupportClick = {
            viewModel.contactSupport(EmailData(
                arrayOf(context.getString(R.string.my_email)),
                context.getString(R.string.support_mail_theme),
                context.getString(R.string.support_mail_body_content)
            ))
        },
        onAgreementClick = {
            viewModel.openTerms(context.getString(R.string.user_agreement_link))
        }
    )
}