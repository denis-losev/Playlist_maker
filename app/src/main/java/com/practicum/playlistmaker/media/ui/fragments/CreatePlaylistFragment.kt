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
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.utils.BindingFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistFragment() :
    BindingFragment<FragmentCreatePlaylistBinding>() {

    private var coverImage: Uri? = null
    private var playlistName: String = ""
    private var playlistDescription: String = ""

    private val playlistsInteractor: PlaylistsInteractor by inject()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreatePlaylistBinding {
        return FragmentCreatePlaylistBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButtonListener()
        setupUI()
    }

    private fun setupUI() {
        binding.editPlaylistName.addTextChangedListener(textInputWatcher)
        binding.editPlaylistDescription.addTextChangedListener(textInputWatcher)

        val cornerRadiusInPx = 8.dpToPx(requireContext())

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

                if (uri != null) {
                    Glide.with(requireContext())
                        .load(uri)
                        .transform(RoundedCorners(cornerRadiusInPx))
                        .into(binding.playlistImage)
                    coverImage = uri
                    updateCreateButtonState(true)
                }
            }

        binding.playlistImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.createButton.setOnClickListener {
            savePlaylist()
        }
    }

    private fun backButtonListener() {
        binding.toolbar.setNavigationOnClickListener {
            if (!playlistName.isNullOrEmpty() || !playlistDescription.isNullOrEmpty() || coverImage != null) {
                showExitDialog()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private val textInputWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {
        }

        override fun onTextChanged(
            input: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            playlistName = binding.editPlaylistName.text?.toString()?.trim().orEmpty()
            playlistDescription = binding.editPlaylistDescription.text?.toString()?.trim().orEmpty()

            updateCreateButtonState(playlistName.isNotEmpty())
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }

    private fun updateCreateButtonState(isFieldNotEmpty: Boolean) = with(binding) {
        when (isFieldNotEmpty) {
            true -> {
                createButton.isClickable = true
                createButton.isEnabled = true
                createButton.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
            }

            false -> {
                createButton.isClickable = false
                createButton.isEnabled = false
                createButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            }
        }
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

    private fun savePlaylist() {
        playlistName = binding.editPlaylistName.text.toString().trim()
        playlistDescription = binding.editPlaylistDescription.text.toString().trim()

        var savedImagePath: String? = null

        if (coverImage != null) {
            savedImagePath = saveImageToPrivateStorage(coverImage!!)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            playlistsInteractor.addPlaylist(
                Playlist(
                    name = binding.editPlaylistName.text.toString().trim(),
                    image = savedImagePath ?: "",
                    description = binding.editPlaylistDescription.text.toString().trim()
                )
            )
            if (playlistName.isNotEmpty()) {
                Toast.makeText(requireContext(), "Плейлист $playlistName создан", Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.is_create_playlist_finished)
            .setMessage(R.string.all_not_saved_changes_will_lost)
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(R.string.finish) { _, _ -> findNavController().popBackStack() }
            .show()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}