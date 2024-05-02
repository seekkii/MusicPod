package com.example.myapplication

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.gson.reflect.TypeToken


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlaylistDialog(
    musicTrackModel: MusicTrackModel,
    showDialog: MutableState<Boolean>,
    currentView: MutableState<ViewType>)
{
    if (!showDialog.value){
        return
    }
    val playlistList = mutableListOf<Int>()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var playlistNameField by remember{
        mutableStateOf(TextFieldValue("Playlist ${musicTrackModel.playlistModel.playListMap.size}"))
    }
    
    var existingNameError by remember {
        mutableStateOf(false)
    }

    var confirmClicked by remember{
        mutableStateOf(false)
    }


    val onDismissRequest = {
        showDialog.value = false
    }

    val onConfirmation = {
        if (musicTrackModel.playlistModel.playListMap[playlistNameField.text] != null){
            existingNameError = true
        }
        else {
            musicTrackModel.playlistModel.playListMap[playlistNameField.text] = playlistList
            musicTrackModel.playlistModel.currentPlaylistName = playlistNameField.text
            confirmClicked = true
            showDialog.value = false
            musicTrackModel.playlistModel.modified = true
        }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        val outlineColor = if (existingNameError) Color.Red else MaterialTheme.colorScheme.primary
        LaunchedEffect(Unit){
            // Request focus once the TextField is positioned
            focusRequester.requestFocus()
            // Show the keyboard
            keyboardController?.show()
        }

        DisposableEffect(Unit){
            onDispose {
                if (confirmClicked){
                    currentView.value = ViewType.PLAYLISTSELECT
                }
            }
        }
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
               ,
            shape = RoundedCornerShape(16.dp),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "New Playlist",
                    modifier = Modifier.padding(16.dp),
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                val text = playlistNameField.text
                                playlistNameField = playlistNameField.copy(
                                    selection = TextRange(0, text.length)
                                )
                            }
                        }

                    ,
                    value = playlistNameField,
                    onValueChange = { playlistNameField = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = outlineColor,
                        //unfocusedBorderColor = Yellow)
                        //placeholder = { Text(text = "e.g. Hexamine") },
                    )
                )
                if(existingNameError) {
                    Text(text = "Playlist name existed, please use another name")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { onConfirmation() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun Popup(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                //.padding(2.dp),
            //shape = MaterialTheme.shapes.medium,
                    ,
            //elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        Color.Black.copy()
                    )
            ) {
                content()
            }
        }
    }
}


@Composable
fun PopupExample() {
    var isPopupVisible by remember { mutableStateOf(false) }

    // Trigger the popup visibility
    Button(
        onClick = { isPopupVisible = true },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Show Popup")
    }
    // Define the popup content
    if (isPopupVisible) {
        Popup(
            onDismissRequest = { isPopupVisible = false },
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        //.background(Color.Magenta)
                        //.border(1.dp, Color.Magenta),
                            ,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Popup Content")
                    Button(
                        onClick = { isPopupVisible = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Popup")
                    }
                }
            }
        )
    }
}


@Composable
fun AddNewPlaylist(newPlaylist: MutableState<Boolean>,
                   width: Dp,
                   currentView: MutableState<ViewType>,
                   musicTrackModel: MusicTrackModel
){
    val painter = painterResource(id = R.drawable.awawa)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable {
                //showSelectedPlaylist.value = true
                newPlaylist.value = true
                //amount.value = amount.value + 1
            }
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(width)
                .background(color = Color.Gray.copy(alpha = 0.5f)), // Set the background color with transparency
            contentAlignment = Alignment.Center // Align the content to the start (left) of the Box
        ) {
            Image(painter = painter, contentDescription = "")
        }
    }

    NewPlaylistDialog(
        musicTrackModel,
        newPlaylist,
        currentView
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayListCheckBox(showPlaylistSong: MutableState<Boolean>,
                 currentView: MutableState<ViewType>,
                 playlistName : String,
                 width:Dp,
                 musicTrackModel: MusicTrackModel)
{

    if (!musicTrackModel.playlistModel.playListMap.containsKey(playlistName)) {
        return
    }

    var checkedState by remember { mutableStateOf(false) }
    val showPlaylistUltility = musicTrackModel.playlistModel.showPlaylistUltility.observeAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.height(width)
            .padding(8.dp)
            .background(
                Color.Transparent
            )
            .combinedClickable(
                onClick = {
                    showPlaylistSong.value = true
                    musicTrackModel.playlistModel.currentPlaylistName = playlistName
                },
                onLongClick = {
                    musicTrackModel.playlistModel.showPlaylistUltility.value = true
                    checkedState = true
                },
            )
        , verticalAlignment = Alignment.CenterVertically

    ) {
        if (showPlaylistUltility.value == true) {
            CircleCheckbox(selected = checkedState, Modifier.size(width)) {
                checkedState = !checkedState
                if (checkedState) {
                    musicTrackModel.playlistModel.playlistDeleteList.add(playlistName)
                } else {
                    musicTrackModel.playlistModel.playlistDeleteList.remove(playlistName)
                }
            }
        }
            Box(
                modifier = Modifier
                    .width(width)
                    .height(width)
                    .background(color = Color.Gray.copy(alpha = 0.5f)), // Set the background color with transparency
                contentAlignment = Alignment.Center // Align the content to the start (left) of the Box
            ) {
                CoverItem(path = null, musicTrackModel)
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = playlistName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }


@Composable
fun CircleCheckbox(selected: Boolean, modifier: Modifier, onChecked: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    val tint = if (selected) color.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
    val background = if (selected) Color.White else Color.Transparent
    Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onChecked
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .background(color = background, shape = CircleShape)

    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                tint = tint,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "checkbox"
            )
        }
    }
}

@Composable
fun Playlist(makeNewPlaylist: MutableState<Boolean>,
             showPlaylistSong : MutableState<Boolean>,
             currentView: MutableState<ViewType>,
             musicTrackModel: MusicTrackModel) {
    val context = LocalContext.current
    val musicItemHeight by remember {
        mutableStateOf(60.dp)
    }


    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        item{
            AddNewPlaylist(makeNewPlaylist, musicItemHeight, currentView, musicTrackModel)
        }
        musicTrackModel.playlistModel.playListMap.keys.forEach() { name ->
            item() {
                PlayListCheckBox(
                    showPlaylistSong,
                    currentView,
                    name,
                    musicItemHeight,
                    musicTrackModel)
            }

        }
        //Log.d("",playlistList.value.size.toString())
    }
}

@Composable
fun SelectedPlaylist(
    showSelectedPlaylist: MutableState<Boolean>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection : MediaPlayerServiceConnection)
{
    BackHandler {
        showSelectedPlaylist.value = false
    }
    if (!showSelectedPlaylist.value){
        return
    }
    val mp3FilesList = lastTrackModel.mp3fileList
    val playlistList = remember{
        mutableStateOf(mp3FilesList.let { lastTrackModel.playlistModel.playlistToMp3List(it) })
    }
    SongLazyList(playlistList.value, lastTrackModel , mediaPlayerServiceConnection ){}
}


@Composable
fun SongCheckBox(musicTrackModel: MusicTrackModel,
                 track: MusicTrack){
    val context = LocalContext.current

    val density = LocalDensity.current.density
    val screenWidthDp = (LocalConfiguration.current.screenWidthDp).dp
    val screenHeightDp = (LocalConfiguration.current.screenHeightDp).dp
    val musicItemHeight = screenHeightDp / 15
    val betweenItemPadding = screenHeightDp / 100
    // Create a mutable state to hold the checked state of the checkbox
    var checkedState by remember { mutableStateOf(false) }
    val checkAll = musicTrackModel.playlistModel.checkAll.observeAsState()

    LaunchedEffect(checkAll.value){
        checkAll.value?.let { checkAllValue ->
            checkedState = checkAllValue
        }
    }

    Box(modifier = Modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.height(musicItemHeight)
                .padding(betweenItemPadding)
                .clickable {
                    checkedState = !checkedState
                }
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleCheckbox(selected = checkedState, Modifier.padding(20.dp)) {
                Log.d("playlist", musicTrackModel.playlistModel.selectedAmountOfSong.value.toString())
                if (checkedState) {
                    musicTrackModel.playlistModel.getPlaylistList().remove(track.id)
                    musicTrackModel.playlistModel.selectedAmountOfSong.value = musicTrackModel.playlistModel.selectedAmountOfSong.value!! - 1

                } else {
                    musicTrackModel.playlistModel.getPlaylistList().add(track.id)
                    musicTrackModel.playlistModel.selectedAmountOfSong.value = musicTrackModel.playlistModel.selectedAmountOfSong.value!! + 1
                }
                checkedState = !checkedState
            }
            MusicItem(path = track.cover, musicItemHeight, Modifier,musicTrackModel)
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "${track.title}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun SongToSelectList(
    currentView: MutableState<ViewType>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    musicTrackModel: MusicTrackModel,
) {
    val playlistModel = musicTrackModel.playlistModel
    val name = playlistModel.currentPlaylistName
    BackHandler {
        currentView.value = ViewType.HOME
    }
    ProvideWindowInsets {
        Column(
            Modifier.padding(
                rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars
                )
            ),
            )
        {
            val selecting by remember {
                if (playlistModel.playListMap[name]?.size==0){
                    mutableStateOf(true)
                }else{
                    mutableStateOf(false)
                }
            }
            //Log.d("playlist","${selecting} $name ${musicTrackModel.playListMap[name]}")
            SelectAll(musicTrackModel, currentView)
            LazyColumn(state = rememberLazyListState(),)
            {
                mp3FilesList.value.forEach { track ->
                    item {
                        SongCheckBox(musicTrackModel, track) }
                    }
                }
            }
    }

    DisposableEffect(Unit){
        onDispose {
            musicTrackModel.playlistModel.checkAll.value = false
        }
    }
}

@Composable
fun SelectAll(musicTrackModel: MusicTrackModel, currentView: MutableState<ViewType>){
    var checkedState by remember { mutableStateOf(false) }
    val songsSelected = musicTrackModel.playlistModel.selectedAmountOfSong.observeAsState()

    Box(modifier = Modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.height(musicItemHeight)
                .clickable {

                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            )
            {
                CircleCheckbox(selected = checkedState, Modifier.padding(20.dp)) {
                    checkedState = !checkedState
                    musicTrackModel.playlistModel.checkAll.value = checkedState
                    if (checkedState){
                        val mp3FilesList = musicTrackModel.mp3fileList
                        musicTrackModel.playlistModel.selectedAmountOfSong.value = mp3FilesList.size
                        val currentPlaylist = musicTrackModel.playlistModel.currentPlaylistName
                        musicTrackModel.playlistModel.playListMap[currentPlaylist] =  MutableList(mp3FilesList.size) { it }
                    }else{
                        val currentPlaylist = musicTrackModel.playlistModel.currentPlaylistName
                        musicTrackModel.playlistModel.playListMap[currentPlaylist] = mutableListOf()
                    }
                }
                Modifier
                    .fillMaxWidth(0.5f)
                //.background(Color.Blue)
                //, contentAlignment = Alignment.TopStart
                //) {
                Text(text = "Select tracks")

            }

            IconButton(onClick = {
                if (songsSelected.value!! > 0){
                    currentView.value = ViewType.NAVIGATION
                }

            }) {
                val imageVector = if (songsSelected.value!! > 0) {
                    Icons.Default.Check
                } else {
                    Icons.Filled.Search
                }
                Icon(imageVector, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun selectSongsBar(){

}
