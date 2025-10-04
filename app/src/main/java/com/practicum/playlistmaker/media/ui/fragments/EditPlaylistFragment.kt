package com.practicum.playlistmaker.media.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.Constants.PLAYLIST
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistViewModel
import com.practicum.playlistmaker.utils.BindingFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException

class EditPlaylistFragment : BindingFragment<FragmentCreatePlaylistBinding>() {

    private val viewModel: PlaylistViewModel by viewModel()
    private lateinit var playlist: Playlist
    private var coverImage: Uri? = null
    private var hasUnsavedChanges = false

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val cornerRadiusInPx = 8.dpToPx(requireContext())
                Glide.with(requireContext())
                    .load(uri)
                    .transform(RoundedCorners(cornerRadiusInPx))
                    .into(binding.playlistImage)
                coverImage = uri
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreatePlaylistBinding {
        return FragmentCreatePlaylistBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlist = arguments?.getParcelable(PLAYLIST)
            ?: throw IllegalArgumentException("playlist is required")

        setupUI()
        setupClickListeners()
        setupTextWatchers()
        setupBackButtonListener()
    }

    private fun setupUI() {
        with(binding) {
            toolbar.title = getString(R.string.edit_playlist)

            createButton.text = getString(R.string.save)

            editPlaylistName.setText(playlist.name)
            editPlaylistDescription.setText(playlist.description)

            Glide.with(requireContext())
                .load(playlist.image)
                .centerCrop()
                .transform(RoundedCorners(8))
                .placeholder(R.drawable.playlist_image_empty)
                .into(playlistImage)

            updateSaveButtonState()
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            createButton.setOnClickListener {
                savePlaylistChanges()
            }
            playlistImageCard.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun setupBackButtonListener() {
        binding.toolbar.setNavigationOnClickListener {
            if (hasUnsavedChanges) {
                showExitDialog()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupTextWatchers() {
        binding.editPlaylistName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        })

        binding.editPlaylistDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        })
    }

    private fun updateSaveButtonState() {
        val name = binding.editPlaylistName.text.toString().trim()
        val description = binding.editPlaylistDescription.text.toString().trim()

        val hasChanges = hasChanges(name, description) || coverImage != null
        val isValid = name.isNotEmpty()

        binding.createButton.isEnabled = hasChanges && isValid

        if (binding.createButton.isEnabled) {
            binding.createButton.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
        } else {
            binding.createButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        }
    }

    private fun hasChanges(newName: String, newDescription: String): Boolean {
        return newName != playlist.name || newDescription != playlist.description
    }

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        return try {
            val filePath = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "coverImages"
            )

            if (!filePath.exists()) filePath.mkdirs()

            val timestamp = System.currentTimeMillis()
            val fileName = "playlist_cover_${timestamp}.jpg"
            val file = File(filePath, fileName)

            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
                }
            }
            file.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun savePlaylistChanges() {
        val newName = binding.editPlaylistName.text.toString().trim()
        val newDescription = binding.editPlaylistDescription.text.toString().trim()

        if (newName.isEmpty()) {
            return
        }

        var savedImagePath: String? = playlist.image

        if (coverImage != null) {
            savedImagePath = saveImageToPrivateStorage(coverImage!!)
        }

        val updatedPlaylist = playlist.copy(
            name = newName,
            description = newDescription,
            image = savedImagePath ?: playlist.image
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = viewModel.updatePlaylist(updatedPlaylist)
                if (success) {

                    Toast.makeText(
                        requireContext(),
                        "Изменения сохранены",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        PLAYLIST,
                        updatedPlaylist
                    )

                    findNavController().popBackStack()
                } else {
                    showErrorDialog("Не удалось сохранить изменения")
                }
            } catch (e: Exception) {
                showErrorDialog("Ошибка при сохранении: ${e.message}")
            }
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выйти без сохранения?")
            .setMessage("Все несохраненные изменения будут потеряны")
            .setNeutralButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Выйти") { _, _ -> findNavController().popBackStack() }
            .show()
    }

    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        fun newInstance(playlist: Playlist): EditPlaylistFragment {
            return EditPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST, playlist)
                }
            }
        }
    }
}