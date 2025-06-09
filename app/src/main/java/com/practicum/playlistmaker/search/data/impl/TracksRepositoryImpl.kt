package com.practicum.playlistmaker.search.data.impl

import com.practicum.playlistmaker.search.Resource
import com.practicum.playlistmaker.search.data.network.NetworkClient
import com.practicum.playlistmaker.search.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.search.data.dto.TracksSearchRequest
import com.practicum.playlistmaker.search.domain.TracksRepository
import com.practicum.playlistmaker.search.domain.model.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(searchText: String): Resource<List<Track>> {
        return try {
            val response = networkClient.doRequest(TracksSearchRequest(searchText))

            return when (response.resultCode) {
                -1 -> Resource.Error("Отсутствует подключение к интернету")
                200 -> {
                    Resource.Success(
                        (response as TrackSearchResponse).results.map {
                            Track(
                                it.trackId,
                                it.trackName,
                                it.artistName,
                                it.trackTimeMillis,
                                it.artworkUrl100,
                                it.collectionName,
                                it.releaseDate,
                                it.primaryGenreName,
                                it.country,
                                it.previewUrl
                            )
                        })
                }

                else -> Resource.Error("Ошибка сервера")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Произошла ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}")
        }

    }
}