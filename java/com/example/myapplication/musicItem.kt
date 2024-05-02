package com.example.myapplication

import android.graphics.fonts.FontFamily
import android.graphics.fonts.FontStyle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Utility function to format the duration in hh:mm:ss format
fun formatDuration(durationMs: Long?): String {
    if (durationMs == null || durationMs < 0 ) {
        return  "00:00"
    }

    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}



@Composable
fun EnlargeTextBoxOncick(text:String, color: Color) {
    val defaultFontSize = MaterialTheme.typography.bodyLarge.fontSize

    var fontSize by remember {
        mutableStateOf (defaultFontSize)
    }
    Box(modifier = Modifier
        .clickable {
            fontSize *= 2
            if (fontSize > defaultFontSize * 2) {
                fontSize = defaultFontSize
            }
            Log.d("textEnlarge", "enlarge text")
        },
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = text,
            color = color,
            fontSize = fontSize
        )
    }
}

/*
@Composable
fun CenteredClickableBox(text: String, modifier: Modifier = Modifier) {
    var fontSize by remember { mutableStateOf(16.sp) }
    val defaultFontSize = 16.sp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .clickable(
                indication = rememberRipple(color = Color.Gray),
                onClick = {
                    fontSize *= 2
                    if (fontSize > defaultFontSize * 2) {
                        fontSize = defaultFontSize
                    }
                    Log.d("textEnlarge", "enlarge text")
                }
            )
            .then(modifier)
            .align(Alignment.Center)
    ) {
        Surface(
            modifier = Modifier,
            color = Color.Gray,
            contentColor = contentColorFor(Color.Gray)
        ) {
            // Your Text content
            Text(
                text = text,
                color = Color.White,
                fontSize = fontSize
            )
        }
    }
}

 */

@Composable
fun SongLazyList(
    mp3FilesList:List<MusicTrack>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    utility:@Composable ()->Unit
){
    val context = LocalContext.current
    val musicItemHeight by remember {
        mutableStateOf(60.dp)
    }
    val lastId by lastTrackModel.lastTrackId.observeAsState()
    LazyColumn(
        state = rememberLazyListState(), // Pass the LazyListState to the LazyColumn
    ) {
        item {
            utility()
        }

        mp3FilesList.forEach{track ->
            item {
                val isSelected = if (lastId == track.id ){
                    Color.Black.copy(0.5f)
                }else{
                    Color.Transparent
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(isSelected)
                        .clickable {
                            mediaPlayerServiceConnection.setIndex(track.id)
                            mediaPlayerServiceConnection.play()
                            lastTrackModel.currentSongPosition.value = 0L

                        }
                ) {
                    MusicItem(track.cover, musicItemHeight, Modifier.background(isSelected), lastTrackModel)
                    Column(
                        Modifier
                            .height(musicItemHeight)
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MusicItemTittleAndArtist(Modifier, track)
                    }
                }
            }
        }
    }
}

@Composable
fun MusicItemTittleAndArtist(modifier: Modifier, track: MusicTrack) {
    Box {
        Text(
            modifier = Modifier,//.padding(start = 4.dp),
            text = "${track.title}",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Box {
        Text(
            modifier = Modifier,//.padding(start = 4.dp),
            text = "${track.artist}",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MusicItemList(
    currentView: MutableState<ViewType>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection : MediaPlayerServiceConnection,
    showSearchBar: MutableState<Boolean>,

    ) {
    val context = LocalContext.current
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            delay(1000)
            launch {
                withContext(Dispatchers.IO) {
                    mp3FilesList.value = Mp3Scanner.scanForMp3Files(context, mp3FilesList.value)
                }
                lastTrackModel.mp3fileList = mp3FilesList.value
            }
            refreshState.endRefresh()
        }
    }

    Column {
        Box(Modifier.nestedScroll(refreshState.nestedScrollConnection)) {
            SongLazyList(mp3FilesList.value, lastTrackModel, mediaPlayerServiceConnection){
                OrderUltility(mp3FilesList, lastTrackModel, mediaPlayerServiceConnection)
            }
                PullToRefreshContainer(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .alpha(refreshState.verticalOffset.coerceIn(0f, 1f)),
                    state = refreshState,
                )
        }
    }
    // AnimatedVisibility here for animations
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut()
    ) {
        // Your content for the AnimatedVisibility
    }

    /*DisposableEffect(key1 = currentPlayingIndex.value) {
        onDispose {
            currentPlayingIndex.value?.let { saveCurrentPlayingIndex(context, it) }
        }
    }
     */
}

fun mapValue(value: Float, fromStart: Float, fromEnd: Float, toStart: Float, toEnd: Float): Float {
    return (value - fromStart) / (fromEnd - fromStart) * (toEnd - toStart) + toStart
}


@Composable()
fun OrderUltility(mp3FilesList: MutableState<List<MusicTrack>>,
                  lastTrackModel: MusicTrackModel,
                  mediaPlayerServiceConnection: MediaPlayerServiceConnection
){
    var showSortDropDown by remember { mutableStateOf(false) }
    var sortKey by remember {
        mutableStateOf(lastTrackModel.sortOrder)
    }
    var starSorting by remember {
        mutableStateOf(false)
    }
    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .height(25.dp)
        )
    {
        val (sort, space, orderType) = createRefs()
        Row(Modifier
            .constrainAs(sort){
                start.linkTo(parent.start)
            }, verticalAlignment = Alignment.CenterVertically

        ){
            IconButton(onClick = { showSortDropDown = !showSortDropDown}) {
                Icon(Icons.AutoMirrored.Default.List, null, tint = Color.White.copy(0.5f))
                DropdownMenu(
                    expanded = showSortDropDown,
                    onDismissRequest = { showSortDropDown = false }
                ) {
                    DropdownMenuItem({ Text("Title") },onClick = {
                        sortKey = "Title"
                        starSorting = true
                        showSortDropDown = false
                    })
                    DropdownMenuItem({ Text("Artist") },onClick = {
                        sortKey = "Artist"
                        starSorting = true
                        showSortDropDown = false
                    })
                    DropdownMenuItem({ Text("Last added") },onClick = {
                        sortKey = "Last added"
                        starSorting = true
                        showSortDropDown = false
                    })

                    if(starSorting) {
                        LaunchedEffect(sortKey) {
                            launch {
                                withContext(Dispatchers.IO) {
                                    lastTrackModel.sortOrder = sortKey
                                    mp3FilesList.value = lastTrackModel.sortMp3FileList(sortKey)
                                }
                                //Log.d("LastIndex",lastTrackModel.lastTrackId.value.toString())
                                var lastTrackModified = false
                                mp3FilesList.value.forEachIndexed{index, musicTrack ->
                                    if (!lastTrackModified && lastTrackModel.lastTrackId.value == musicTrack.id){
                                        lastTrackModel.lastTrackId.value = index
                                        //lastTrackModel.currentTrack.value?.id = index
                                        mediaPlayerServiceConnection.setIndex(index)
                                        Log.d("LastIndexAfterSorted","${lastTrackModel.lastTrackId.value} ${musicTrack.id} ${index}")
                                        lastTrackModified = true
                                    }
                                    musicTrack.id = index
                                }
                                mediaPlayerServiceConnection.updatemp3FilesList(mp3FilesList.value)
                                //lastTrackModel.lastTrackId.value = lastTrackModel.currentTrack.value?.id
                                starSorting = false
                            }
                        }
                    }
                }
            }
            // space and order type
            Text(text = sortKey,
                color = Color.White.copy(0.5f),
                fontStyle = MaterialTheme.typography.titleMedium.fontStyle
            )
        }
        Row(
            Modifier.constrainAs(orderType){
                end.linkTo(parent.end)
            }
        ){
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, " ArrowBack description")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.ArrowDropDown, "ArrowDropDown description")
            }
        }
    }
}

@Composable
fun CustomSlider1(offset: Float,
                  lastTrackModel: MusicTrackModel,
                  duration: Long,
                  distance : MutableState<Long>,
                  scrollState: ScrollableState
) {
    val color = MaterialTheme.colorScheme.primary.copy(0.5f).value
    val currentPosition = lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)
    Log.d("pos currently custom slider", currentPosition.value.toString())

    var sliderHeight by remember {
        mutableFloatStateOf(0f)
    }
    Box(
        modifier = Modifier
            .padding(11.dp)
            .clip(RoundedCornerShape(10.dp))
            .height(6.dp)
            .fillMaxWidth()
            .graphicsLayer(
                //translationY = visibilityPadding
                //translationX = animateFloatAsState ((size.width * percentage).toDp()).value
            )
            .background(Color.Black.copy(0.5f))
            .onGloballyPositioned {
                sliderHeight = it.size.height.toFloat()
                distance.value = duration / it.size.width
            }
            .drawBehind {
                drawRoundRect(
                    Color(color),
                    Offset(0f, 0f),
                    size = Size(size.width * currentPosition.value / duration, size.height),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
    )
}
//}

@Composable
fun CustomSlider(offset: Float,
                 animatedPercentage :Float,
                 position : Long,
                 duration: Long,
                 distance : MutableState<Long>,
                 scrollState: ScrollableState
) {
    val color = MaterialTheme.colorScheme.primary.copy(0.5f).value

    var sliderHeight by remember {
        mutableFloatStateOf(0f)
    }
    /*
    val visibilityPadding by animateFloatAsState(
        targetValue =
        if (scrollState.isScrollInProgress) {
            0f
        }
        else {
            sliderHeight
        },
        label = ""
    )
     */
    //AnimatedVisibility(scrollState.isScrollInProgress) {
        Box(
            modifier = Modifier
                //.padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .height(3.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    //translationY = visibilityPadding
                    //translationX = animateFloatAsState ((size.width * percentage).toDp()).value
                )
                .background(Color.Black.copy(0.5f))
                .onGloballyPositioned {
                    sliderHeight = it.size.height.toFloat()
                    distance.value = duration / it.size.width
                }
                .drawBehind {
                    drawRoundRect(
                        Color(color),
                        Offset(0f, 0f),
                        size = Size(size.width * offset/duration, size.height),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
        )
    }
//}

@Composable
fun MusicPlayerScreen(
    showTrackWaitUI: MutableState<Boolean>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection : MediaPlayerServiceConnection,
    modifier: Modifier = Modifier,

) {
    val context = LocalContext.current
    val exoInstance = mediaPlayerServiceConnection.getExoInstance()
    val currentPosition by lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)
    val currentDuration by lastTrackModel.currentSongDuration.observeAsState(initial = 0)

    val distance = remember {
        mutableLongStateOf(1)
    }
    var offset by remember { mutableLongStateOf(lastTrackModel.initialPosition) }
    val scrollState = rememberScrollableState { delta ->
            offset.let {
                if (it < 0){
                    offset = 0
                } else if (it > currentDuration) {
                    offset = currentDuration
                } else {
                    offset += (delta * distance.longValue).toLong()
                }
            }
        delta
    }
    if (scrollState.isScrollInProgress){
        DisposableEffect(Unit){
            onDispose {
                exoInstance?.seekTo(offset)
                lastTrackModel.currentSongPosition.value = offset
                    //offset = currentPosition.value.toFloat()
            }
        }
    }
    LaunchedEffect(lastTrackModel.lastTrackId.value){
        offset = currentPosition
        //Log.d("new position and duration", "${offset} $duration")
    }
    val currentTrack = lastTrackModel.currentTrack.value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .clickable {
                showTrackWaitUI.value = true
            }
            .scrollable(
                orientation = Orientation.Horizontal,
                state = scrollState
            )) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            ) {
                TrackUtilityCompose(
                    showTrackWaitUI = showTrackWaitUI,
                    lastTrackModel = lastTrackModel,
                    trackPosition = offset,
                    trackDuration = currentDuration,
                    mediaPlayerServiceConnection = mediaPlayerServiceConnection,
                    scrollState = scrollState
                )

            Box(modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                ) {
                CoverItem(
                    path = currentTrack?.cover,
                    lastTrackModel,
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }
        }
        TrackUtilityBar(offset,currentDuration, lastTrackModel,scrollState)

        CustomSlider1(
            offset.toFloat(),
            lastTrackModel,
            currentDuration,
            distance,
            scrollState
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PlayPauseBar(lastTrackModel, mediaPlayerServiceConnection )
            val iconSize = 24.dp

            var favorite by remember {
                mutableStateOf( currentTrack?.let {(it.fav) }?:false)
            }
            currentTrack?.let {
                LaunchedEffect(currentTrack){
                    favorite = currentTrack.fav
                }
                IconButton(
                    onClick = {
                        favorite = !favorite
                        currentTrack.fav = favorite
                    }
                ) {
                    Image(
                        painter = painterResource(
                            if (favorite) {
                                R.drawable.heartfilled
                            } else {
                                R.drawable.heart
                            }
                        ),
                        contentDescription = "Heart Icon",
                        modifier = Modifier
                            .size(iconSize),
                        //colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun MusicPlayerScreen2(
    transparency: MutableState<Float>,
    showTrackWaitUI: MutableState<Boolean>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection : MediaPlayerServiceConnection
) {
    val exoInstance = mediaPlayerServiceConnection.getExoInstance()
    val currentPosition by lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)
    val currentDuration by lastTrackModel.currentSongDuration.observeAsState(initial = 0)

    val distance = remember {
        mutableLongStateOf(1)
    }
    var offset by remember { mutableLongStateOf(lastTrackModel.initialPosition) }
    var initialSet by remember { mutableStateOf(true) }
    var animatedPercentage by remember{
        mutableFloatStateOf(currentPosition / currentDuration.toFloat())
    }
    var sliderPosIsChanging by remember{
        mutableStateOf(true)
    }
    val scrollState = rememberScrollableState { delta ->
        offset.let {
            if (it < 0){
                offset = 0
            } else if (it > currentDuration) {
                offset = currentDuration
            } else {
                offset += (delta * distance.longValue).toLong()
            }
        }
        animatedPercentage = offset/currentDuration.toFloat()
        delta
    }

    val sliderOffset =
        if (scrollState.isScrollInProgress){
            if (sliderPosIsChanging){
                offset = currentPosition
                sliderPosIsChanging = false
            }
            DisposableEffect(Unit){
                onDispose {
                    exoInstance?.seekTo(offset)
                    lastTrackModel.currentSongPosition.value = offset
                    sliderPosIsChanging = true
                }
            }
            offset
        }
        else if (sliderPosIsChanging) {
            currentPosition
        }
        else{
            offset
        }

    LaunchedEffect(lastTrackModel.lastTrackId.value){
        if (initialSet){
            initialSet = false
        }
        else {
            offset = 0
            animatedPercentage = 0f
        }
    }

    val currentTrack = lastTrackModel.currentTrack.value
    Column(
        modifier = Modifier
            .background(Color.Black.copy(0.5f))
            .fillMaxWidth()
            .scrollable(
                orientation = Orientation.Horizontal,
                state = scrollState
            )
            .clickable {
                showTrackWaitUI.value = true
            }
    )
    {
        Row(
            Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
            ) {
                CoverItem(
                    path = currentTrack?.cover,
                    lastTrackModel,
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }
            TrackUtilityCompose(
                showTrackWaitUI = showTrackWaitUI,
                lastTrackModel = lastTrackModel,
                trackPosition = offset,
                trackDuration = currentDuration,
                mediaPlayerServiceConnection = mediaPlayerServiceConnection,
                scrollState = scrollState
            )
            PlayPauseBar(lastTrackModel, mediaPlayerServiceConnection)
        }

        CustomSlider(
           sliderOffset.toFloat(),
            animatedPercentage,
            currentPosition,
            currentDuration,
            distance,
            scrollState
        )
        Row(Modifier.fillMaxWidth(),) {
            var favorite by remember {
                mutableStateOf( currentTrack?.let {(it.fav) }?:false)
            }
            currentTrack?.let {
                LaunchedEffect(currentTrack){
                    favorite = currentTrack.fav
                }
            }
        }
    }

}


@Composable
fun TrackUtilityCompose(showTrackWaitUI: MutableState<Boolean>,
                        trackPosition : Long,
                        trackDuration: Long,
                        lastTrackModel: MusicTrackModel,
                        mediaPlayerServiceConnection : MediaPlayerServiceConnection,
                        scrollState: ScrollableState){

    val currentTrack = lastTrackModel.currentTrack.value
    val isPlaying = remember {
        mutableStateOf(true)
    }
    Column(
        Modifier
            .fillMaxWidth(0.5f)
            .padding(start = 10.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    )

     {
        currentTrack?.title?.let {
            MarqueeText(
                0,
                showTrackWaitUI.value,
                isPlaying.value,
                it.take(90), Modifier.fillMaxWidth())
        }
        currentTrack?.artist?.let {
                Text(
                    it,
                    Modifier.padding(bottom = 4.dp),
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }

}
@Composable
fun CoverItem(path: String?,
              lastTrackModel:MusicTrackModel,
              modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val painter = if (path != null){
        rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(path)
                .memoryCacheKey(path)
                .allowHardware(false)
                .build(),
            lastTrackModel.imageLoader,
        )
    }else{
        painterResource(id = R.drawable.musicnote)
    }

    Image(
        painter = painter,
        contentDescription = "cover",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    )
}

@Composable
fun TrackUtilityBar(
    offset: Long,
    currentDuration : Long,
    lastTrackModel: MusicTrackModel,
    scrollState: ScrollableState,
){
    val currentPosition =
        lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)

    val iconSize = 24.dp

    val currentTrack = lastTrackModel.currentTrack.value
    var favorite by remember {
        mutableStateOf( currentTrack?.let {(it.fav) }?:false)
    }
    if (currentTrack != null) {
        LaunchedEffect(currentTrack){
            favorite = currentTrack.fav
        }
    }

    Column(
        modifier = Modifier
            .height(iconSize)
            .padding(start = 8.dp, end = 8.dp)
        ,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        currentPosition.value?.let {
            Row(modifier = Modifier
                    .fillMaxWidth()
                    ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Center both elements vertically
                )
            {
                if (!scrollState.isScrollInProgress) {
                    Text(formatDuration(it))

                }else{
                    Text(formatDuration(offset))
                    Text(formatDuration(currentDuration))

                }
            }
        }
    }
}


@Composable
fun TrackPositionSlider(lastTrackModel: MusicTrackModel,
                        mediaPlayerServiceConnection : MediaPlayerServiceConnection){
    val exoInstance = mediaPlayerServiceConnection.getExoInstance()
    val currentPosition by
        lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)
    val currentDuration by lastTrackModel.currentSongDuration.observeAsState(0)

    rememberCoroutineScope()
    var valueChangeFinished by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(valueChangeFinished){
        if(valueChangeFinished) {
            exoInstance?.seekTo(currentPosition)
            valueChangeFinished = false
        }
    }

    Slider(
        value = currentPosition.toFloat(),
        onValueChange = { position ->
            // Update the currentPosition while the user drags the Slider
            lastTrackModel.currentSongPosition.value = position.toLong()
        },
        onValueChangeFinished = {
            valueChangeFinished = true
            Log.d("slider","finished")
        }
        ,
        valueRange = 0f..currentDuration.toFloat(),
        modifier = Modifier.padding(8.dp),
    )

}

@Composable
fun PlayPauseBar(lastTrackModel: MusicTrackModel,
                 mediaPlayerServiceConnection : MediaPlayerServiceConnection){

    // Play/Pause Button
    val exoInstance = mediaPlayerServiceConnection.getExoInstance()
    val iconSize = 24.dp
    val isPlaying = lastTrackModel.isPlaying.observeAsState(false)
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = {
            mediaPlayerServiceConnection.playNextTrack(-1)
        }) {
            Image(
                painter = painterResource(R.drawable.fast_forward),
                contentDescription = "Fast Forward Icon",
                modifier = Modifier
                    // Use the flip modifier to flip the icon horizontally
                    .scale(-1f, 1f)
                    // Set other modifiers if needed, like size or padding
                    .size(24.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
        val playButtonImage = if (isPlaying.value == true) {
            painterResource(R.drawable._11871_pause_icon)
        } else {
            painterResource(R.drawable.play_button)
        }

        IconButton(
            onClick = {
                if (!isPlaying.value!!) {
                    if (lastTrackModel.initialPosition == lastTrackModel.currentSongPosition.value) {
                        exoInstance?.seekTo(lastTrackModel.initialPosition)
                    }
                    exoInstance?.play()
                } else {
                    exoInstance?.pause()
                }
                lastTrackModel.isPlaying.value = !lastTrackModel.isPlaying.value!!
            }
        ) {
            Image(
                painter = playButtonImage,
                contentDescription = if (isPlaying.value == true) "Pause Track Icon" else "Play Track Icon",
                modifier = Modifier.size(iconSize),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }

        IconButton(
            onClick = {
                exoInstance?.let {
                    mediaPlayerServiceConnection.playNextTrack(1)
                }
            }
        ) {
            Image(
                painter = painterResource(R.drawable.fast_forward),
                contentDescription = "Forward Track Icon",
                modifier = Modifier
                    .size(24.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }

}


@Composable
fun MusicItem(path: String?,
              boxHeight: Dp,
              modifier: Modifier,
              lastTrackModel: MusicTrackModel,
              ){
    Box(modifier = Modifier
        .width(boxHeight)
        .height(boxHeight)
        .clip(RoundedCornerShape(8.dp))
        //.then(modifier)
       ,
        contentAlignment = Alignment.Center
    ) {
        CoverItem(path = path, lastTrackModel)
    }
}

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier,
    gradientEdgeColor: Color = Color.White,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = MaterialTheme.typography.headlineSmall,
){
    Text(
        text,
        textAlign = textAlign,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = 1,
        onTextLayout = onTextLayout,
        style = style,
    )
}

@Composable
fun MarqueeText(
    context:Byte,
    shown:Boolean,
    exoIsPlaying : Boolean,
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    gradientEdgeColor: Color = Color.White,
    color: Color = Color.White,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {

    val createText = @Composable { localModifier: Modifier ->
        Text(
            text,
            textAlign = textAlign,
            modifier = localModifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = 1,
            onTextLayout = onTextLayout,
            style = style,
        )
    }

    var offset by remember { mutableIntStateOf(0) }
    val textLayoutInfoState = remember { mutableStateOf<TextLayoutInfo?>(null) }
    var lastText by remember { mutableStateOf(text) } // Track the last text value


    LaunchedEffect(textLayoutInfoState.value, shown, exoIsPlaying) {
        val textLayoutInfo = textLayoutInfoState.value
        if (textLayoutInfo == null) {
            // Do something when textLayoutInfo is null
            offset = 0
            return@LaunchedEffect
        }
        if (textLayoutInfoState.value != null && lastText != text) {
            // If the text is a marquee text and has changed, reset the offset to 0
            offset = 0
            lastText = text
        }
        if (textLayoutInfo.textWidth > textLayoutInfo.containerWidth) {
            val running =
                if (context.toInt() == 1) {
                    !shown
                }
                else{
                    shown
                }

            val duration = 7500 * textLayoutInfo.textWidth / textLayoutInfo.containerWidth
            val delay = 1000L
            do {
                val animation = TargetBasedAnimation(
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = duration,
                            delayMillis = 1000,
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    typeConverter = Int.VectorConverter,
                    initialValue = 0,
                    targetValue = -textLayoutInfo.textWidth
                )
                val startTime = withFrameNanos { it }
                do {
                    val playTime = withFrameNanos { it } - startTime
                    offset = (animation.getValueFromNanos(playTime))
                } while (!animation.isFinishedFromNanos(playTime) && !running)
                delay(delay)

            } while (!running)
        }
        /*
        else{
            offset = 100 //(textLayoutInfo.containerWidth - textLayoutInfo.textWidth)/2
        }

         */
    }
    SubcomposeLayout(
        modifier = modifier.clipToBounds()
    ) { constraints ->
        val infiniteWidthConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)
        var mainText = subcompose(MarqueeLayers.MainText) {
            createText(textModifier)
        }.first().measure(infiniteWidthConstraints)

        var gradient: Placeable? = null

        var secondPlaceableWithOffset: Pair<Placeable, Int>? = null
        if (mainText.width <= constraints.maxWidth) {
            mainText = subcompose(MarqueeLayers.SecondaryText) {
                createText(textModifier)
            }.first().measure(constraints)
            textLayoutInfoState.value = null
        } else {
            val spacing = constraints.maxWidth * 2 / 3
            textLayoutInfoState.value = TextLayoutInfo(
                textWidth = mainText.width + spacing,
                containerWidth = constraints.maxWidth
            )
            val secondTextOffset = mainText.width + offset + spacing
            val secondTextSpace = constraints.maxWidth - secondTextOffset
            if (secondTextSpace > 0) {
                secondPlaceableWithOffset = subcompose(MarqueeLayers.SecondaryText) {
                    createText(textModifier)
                }.first().measure(infiniteWidthConstraints) to secondTextOffset
            }
            gradient = subcompose(MarqueeLayers.EdgesGradient) {
                Row {
                    GradientEdge(gradientEdgeColor, Color.Transparent)
                    Spacer(Modifier.weight(1f))
                    GradientEdge(Color.Transparent, gradientEdgeColor)
                }
            }.first().measure(constraints.copy(maxHeight = mainText.height))
        }

        layout(
            width = constraints.maxWidth,
            height = mainText.height
        ) {
            mainText.place(offset, 0)
            secondPlaceableWithOffset?.let {
                it.first.place(it.second, 0)
            }
            gradient?.place(0, 0)
        }
    }
}

@Composable
private fun GradientEdge(
    startColor: Color, endColor: Color,
) {
    Box(
        modifier = Modifier
            .width(10.dp)
        //.fillMaxHeight()
        /*.background(
            brush = Brush.horizontalGradient(
                0f to startColor, 1f to endColor,
            )
        )*/
    )
}

private enum class MarqueeLayers { MainText, SecondaryText, EdgesGradient }
private data class TextLayoutInfo(val textWidth: Int, val containerWidth: Int)




