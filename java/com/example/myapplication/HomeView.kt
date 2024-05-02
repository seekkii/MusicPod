package com.example.myapplication

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun HomeView(
    lastTrackModel: MusicTrackModel,
    showTrackWaitUI: MutableState<Boolean>,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection
){
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        lastTrackModel.currentTrack.value?.let {
            MusicPlayerScreen(
                showTrackWaitUI,
                lastTrackModel,
                mediaPlayerServiceConnection
            )
            Text(text = "")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPager(
    currentView: MutableState<ViewType>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    showSearchBar:MutableState<Boolean>,
    showTrackWaitUI:MutableState<Boolean>,
    pagerState:PagerState,
) {
    val createNewPlaylist = remember {
        mutableStateOf(false)
    }

    val showPlaylistSong = remember{
        mutableStateOf(false)
    }

    val currentDuration by lastTrackModel.currentSongDuration.observeAsState(initial = 0)

    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    lastTrackModel.currentTrack.value?.let {
                        MusicPlayerScreen(
                            showTrackWaitUI,
                            lastTrackModel,
                            mediaPlayerServiceConnection
                        )
                        Text(text = "")
                    }

                }
            }


            2 -> {
                Box(modifier = Modifier.fillMaxSize()){
                    if (showPlaylistSong.value) {
                        SelectedPlaylist(
                            showPlaylistSong,
                            lastTrackModel ,
                            mediaPlayerServiceConnection
                        )
                    }
                    else {
                        Playlist(
                            createNewPlaylist,
                            showPlaylistSong,
                            currentView,
                            lastTrackModel)
                    }
                }
            }

            3 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = "3")
                }
            }


            4 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = "4")
                }
            }

            else -> {
                /*
                SongListView(
                    currentView,
                    uriImg,
                    mp3FilesList ,
                    lastTrackModel,
                    mediaPlayerServiceConnection,
                    onNavigateToSetting = { /*TODO*/ }) {

                 */

                }
            }
        }
    }


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiddleContent(
    currentView: MutableState<ViewType>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    uriImg: MutableState<Uri?>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    showSearchBar: MutableState<Boolean>,
    showTrackWaitUI:MutableState<Boolean>,
    pagerState: PagerState
) {
    val createNewPlaylist = remember {
        mutableStateOf(false)
    }
    val showPlaylistSong = remember{
        mutableStateOf(false)
    }
    val currentDuration by lastTrackModel.currentSongDuration.observeAsState(initial = 0)
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> {
                Box(Modifier.fillMaxSize())
            }
            1 -> {
                MusicItemList(
                    currentView,
                    mp3FilesList ,
                    lastTrackModel,
                    mediaPlayerServiceConnection,
                    showSearchBar)
            }
            2-> PlayListPage(
                currentView ,
                showPlaylistSong ,
                createNewPlaylist ,
                mediaPlayerServiceConnection
            )
            3 ->{
                Box(modifier = Modifier.fillMaxSize())
            }
            4->{
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun PlayListPage(
    currentView: MutableState<ViewType>,
    showPlaylistSong:MutableState<Boolean>,
    createNewPlaylist:MutableState<Boolean>,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection
){

    Box(modifier = Modifier.fillMaxSize()){
        if (showPlaylistSong.value) {
            SelectedPlaylist(
                showPlaylistSong,
                mediaPlayerServiceConnection.musicTrackModel ,
                mediaPlayerServiceConnection
            )
        }
        else {
            Playlist(
                createNewPlaylist,
                showPlaylistSong,
                currentView,
                mediaPlayerServiceConnection.musicTrackModel)
        }
    }
}
@Composable
fun ViewChange(
    currentView: MutableState<ViewType>,
    uriImg: MutableState<Uri?>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    lastTrackModel: MusicTrackModel,
    imageViewModel: ImageViewModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    settingViewModel: MusicPodSettings
)
{
    when (currentView.value) {

        ViewType.THEMECUSTOMIZATIONVIEW -> {
            ThemeCustomization(currentView, imageViewModel, uriImg)
        }

        ViewType.BACKGROUNDPREVIEW -> {
            BackgroundPreview(currentView, imageViewModel, uriImg)
        }

        ViewType.PLAYLISTSELECT ->{
            //Box(Modifier.fillMaxSize())
            SongToSelectList(currentView,mp3FilesList,lastTrackModel)
        }

        ViewType.POPUP ->{
        }

        ViewType.NAVIGATION ->{
            AppNavigation(
                currentView,
                uriImg,
                mp3FilesList,
                lastTrackModel,
                mediaPlayerServiceConnection,
                settingViewModel
            )
        }

        else -> {

        }
    }
}

