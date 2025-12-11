package com.practicum.playlistmaker.search.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.ui.SearchState
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.delay

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@SuppressLint("ResourceAsColor")
@Composable
fun SearchContent(
    state: SearchState,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onClearSearch: () -> Unit,
    onClearHistory: () -> Unit,
    onRefresh: () -> Unit,
    onTrackClick: (Track) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var debouncedQuery by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isDarkTheme = MaterialTheme.colors.isLight.not()

    val textFieldBackgroundColor = if (isDarkTheme) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFFE6E8EB)
    }

    val searchIconColor = if (isDarkTheme) {
        Color(0xFF1A1B22)
    } else {
        Color(0xFFAEAFB4)
    }

    val clearIconColor = if (isDarkTheme) {
        Color(0xFF1A1B22)
    } else {
        Color(0xFFAEAFB4)
    }

    val textColor = Color(0xFF1A1B22)

    val placeholderColor = if (isDarkTheme) {
        Color(0xFF1A1B22).copy(alpha = 0.7f)
    } else {
        Color(0xFFAEAFB4)
    }

    LaunchedEffect(searchQuery) {
        delay(2000L)
        debouncedQuery = searchQuery
        onSearchQueryChanged(debouncedQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {

                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.search),
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 20.sp
                )
            },
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    if (query.isEmpty()) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search),
                        color = placeholderColor
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = searchIconColor
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onClearSearch()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = clearIconColor
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onSearchClicked()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = textColor,
                    backgroundColor = textFieldBackgroundColor,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    placeholderColor = placeholderColor,
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = placeholderColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (state) {
                is SearchState.Loading -> {
                    LoadingView()
                }

                is SearchState.SearchResult -> {
                    TrackList(
                        tracks = state.tracks,
                        onTrackClick = onTrackClick,
                        showHistoryTitle = false
                    )
                }

                is SearchState.History -> {
                    Column {
                        Text(
                            text = stringResource(R.string.search_history_title),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 20.sp
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 72.dp
                            )
                        ) {
                            items(state.tracks) { track ->
                                TrackItem(
                                    track = track,
                                    onTrackClick = { onTrackClick(track) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 76.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    onClearHistory()
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },

                                modifier = Modifier
                                    .height(54.dp),
                                shape = RoundedCornerShape(54.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onPrimary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.clear_history),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                is SearchState.EmptyResult -> {
                    EmptyResultView(message = state.message)
                }

                is SearchState.Error -> {
                    ErrorView(
                        message = state.errorMessage,
                        showRefresh = false,
                        onRefresh = onRefresh
                    )
                }

                SearchState.Init -> Unit
            }
        }
    }
}

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    showHistoryTitle: Boolean = false
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
    ) {
        items(tracks) { track ->
            TrackItem(
                track = track,
                onTrackClick = { onTrackClick(track) }
            )
        }
    }
}

@SuppressLint("ResourceAsColor")
@Composable
fun TrackItem(
    track: Track,
    onTrackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(
                onClick = onTrackClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.artworkUrl100)
                    .crossfade(true)
                    .build(),
                contentDescription = "Track cover",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.cover_placeholder),
                error = painterResource(id = R.drawable.cover_placeholder)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.trackName,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artistName,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ellipse_1),
                        contentDescription = "Ellipse",
                        modifier = Modifier
                            .size(3.dp),
                        tint = Color(R.color.aluminium_snow_color),

                        )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = track.getTrackDuration(),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(id = R.drawable.arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colors.primary,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
fun EmptyResultView(message: UiMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.error404emoji),
            contentDescription = "Error",
            modifier = Modifier.size(120.dp)
        )

        Text(
            text = stringResource(R.string.not_found_error_text),
            color = MaterialTheme.colors.onSurface,
            fontSize = 19.sp,
            modifier = Modifier.padding(vertical = 22.dp)
        )
    }
}

@Composable
fun ErrorView(
    message: UiMessage,
    showRefresh: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.interneterroremoji),
            contentDescription = "Error",
            modifier = Modifier.size(120.dp)
        )

        Text(
            text = stringResource(R.string.internet_error_text),
            color = MaterialTheme.colors.onSurface,
            fontSize = 19.sp,
            modifier = Modifier.padding(vertical = 22.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRefresh,
            shape = RoundedCornerShape(54.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.refresh),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

    }
}