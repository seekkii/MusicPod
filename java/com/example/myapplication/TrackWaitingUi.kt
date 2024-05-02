package com.example.myapplication
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.media3.common.C
import androidx.palette.graphics.Palette
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
private fun DragInteractionSample(currentView: MutableState<ViewType>,
                                  lastTrackModel: MusicTrackModel,
                                  mediaPlayerServiceConnection : MediaPlayerServiceConnection,

) {
    val exoInstance = remember {
        mediaPlayerServiceConnection.getExoInstance()
    }
    val currentPosition =
        lastTrackModel.currentSongPosition.observeAsState(initial = lastTrackModel.initialPosition)
    val currentDuration = lastTrackModel.currentSongDuration.observeAsState()
    val duration = remember { mutableStateOf(currentDuration.value) }


    exoInstance?.let {
        val exoDuration = exoInstance.duration
        Log.d("coverimg","${exoDuration}")
        if (exoDuration != C.TIME_UNSET && exoDuration > 0) {
            duration.value = exoDuration
        }
    }

    val density = LocalDensity.current.density
    val canvasSizeInDp = 300

    val radius by remember {
        mutableFloatStateOf(canvasSizeInDp*density/2)
    }

    var shapeCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var offsetPerSecond = 360.0/(currentDuration.value?.div(1000))!!.toFloat()
    var angle by remember {
        mutableDoubleStateOf((offsetPerSecond * currentPosition.value/1000))
    }

    var angleOffset by remember {
        mutableDoubleStateOf((0.0))
    }


    var offsetX by remember { mutableFloatStateOf(radius) }
    var offsetY by remember { mutableFloatStateOf(0f) }



    Box(
        modifier = Modifier.size(canvasSizeInDp.dp),
        contentAlignment = Alignment.Center
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val interactions = remember { mutableStateListOf<Interaction>() }
        var text by remember { mutableStateOf("") }
        var dragging by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Start -> {
                        text = "Drag Start"
                        dragging = true
                    }

                    is DragInteraction.Stop -> {
                        offsetPerSecond = 360.0/(duration.value?.div(1000))!!.toFloat()
                        val newPosition = (angle*1000/offsetPerSecond).toLong()
                        lastTrackModel.currentSongPosition.value = newPosition
                        exoInstance?.seekTo(newPosition )
                        text = "Drag Stop"
                        dragging = false
                    }

                    is DragInteraction.Cancel -> {
                        text = "Drag Cancel"
                    }
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()

        val modifier = Modifier
            .offset {
                IntOffset(
                    x = offsetX.roundToInt(),
                    y = offsetY.roundToInt()
                )
            }
            .clip(CircleShape)
            .size(13.dp)

            .pointerInput(Unit) {
                var interaction: DragInteraction.Start? = null
                detectDragGestures(
                    onDragStart = {
                        coroutineScope.launch {
                            interaction = DragInteraction.Start()
                            interaction?.run {
                                interactionSource.emit(this)
                            }

                        }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        if (angle < 359.7) {
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            val handleCenter = Offset(offsetX + radius, offsetY + radius)

                            angle = getRotationAngle(handleCenter, shapeCenter)
                            change.consume()
                        } else {
                            val handleCenter = Offset(
                                offsetX + dragAmount.x + radius,
                                offsetY + dragAmount.y + radius
                            )
                            val newAngle = getRotationAngle(handleCenter, shapeCenter)
                            angleOffset = angle - newAngle
                            if (angleOffset > 0 && angleOffset < 0.3) {
                                angle = newAngle
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }

                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            interaction?.run {
                                interactionSource.emit(DragInteraction.Cancel(this))
                            }
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            interaction?.run {
                                interactionSource.emit(DragInteraction.Stop(this))
                            }
                        }
                    }
                )
            }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.10f),
                style = Stroke(10f),
                radius = radius
            )
        }
        Canvas(
            modifier = Modifier
                .size(canvasSizeInDp.dp)
        ) {
            if (!dragging) {
                angle = (offsetPerSecond * currentPosition.value / 1000).toDouble()
            }

            if (angle < 359.5) {
                shapeCenter = center
                val x = (shapeCenter.x + cos(Math.toRadians(angle)) * radius).toFloat()
                val y = (shapeCenter.y + sin(Math.toRadians(angle)) * radius).toFloat()
                offsetX = x - radius
                offsetY = y - radius
            }

            drawArc(
                color = Color.White,
                startAngle = 0f,
                sweepAngle = (angle.toFloat()),
                useCenter = false,
                style = Stroke(10f)
            )

        }

        Surface(
            modifier = modifier,
            interactionSource = interactionSource,
            onClick = {},
            content = {
                /*
                LaunchedEffect(Unit) {

                    //angle = (offsetPerSecond * currentPosition.value / 1000).toDouble()
                    while (true){//angle * 1000/offsetPerSecond < currentPosition.value+1000) {
                        angle += 0.2

                        if (angle >360){
                            angle = 0.0
                        }
                        //val x = (shapeCenter.x + cos(Math.toRadians(angle)) * radius).toFloat()
                        //val y = (shapeCenter.y + sin(Math.toRadians(angle)) * radius).toFloat()
                        //offsetX = x - radius
                       // offsetY = y - radius

                        Log.d("angle", "$angle ${angle * 1000/offsetPerSecond}")
                        delay(200) // Delay for 1 second (1000 milliseconds)
                    }

                }

                 */

            },
            color = Color.White
        )

        Text(
            text = text,
            modifier = Modifier.clickable { currentView.value = ViewType.SONGLISTVIEW }
        )
    }
}


private fun getRotationAngle(currentPosition: Offset, center: Offset): Double {
    val (dx, dy) = currentPosition - center
    val theta = atan2(dy, dx).toDouble()

    var angle = Math.toDegrees(theta)

    if (angle < 0) {
        angle += 360.0
    }
    return angle
}

@Composable
fun Setting(){
    val iconSize by remember{
        mutableStateOf(24.dp)
    }
    val showDropdownOptions = remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            showDropdownOptions.value = !showDropdownOptions.value
        }
    ) {
        Image(
            painter = painterResource(R.drawable._doticon),
            contentDescription = "Forward Track Icon",
            modifier = Modifier
                .size(iconSize),
            colorFilter = ColorFilter.tint(Color.Black)
        )

        if (showDropdownOptions.value) {
            DropdownMenu(
                expanded = showDropdownOptions.value,
                onDismissRequest = { showDropdownOptions.value = false },

                ) {
                DropdownMenuItem({ Text(text = "Settings")},onClick = { })
            }
        }
    }
}

@Composable
fun topBar(modifier: Modifier){
    val iconSize by remember{ mutableStateOf(24.dp) }
    Row(modifier =
        modifier. then(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(20.dp)
            //.background(Color.Black),
        ),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Setting()
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.equalizer),
                contentDescription = "Forward Track Icon",
                modifier = Modifier
                    .size(iconSize),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}

@Composable
fun TrackWaitingWrapper(show: MutableState<Boolean>,
                        currentView: MutableState<ViewType>,
                        lastTrackModel: MusicTrackModel,
                        mediaPlayerServiceConnection : MediaPlayerServiceConnection,
){

    //val isPlaying = lastTrackModel.isPlaying.observeAsState(initial = false)

    val gradientColors = listOf(
        Color(0xFF800080), // Gradient start color deep purple
        //Color(0xFFF4C4F3),//pink
        //Color(0XFFFC67FF),//Sharp Pink
        //Color(0xFFFFC0CB), // Gradient stop color blue
        //Color(0xFFEF32D9),//light purple
        Color(0xFF8474ac),//purple
        //Color(0xFF89FFFD)//light blue
    )
    val mainColor = remember {
        mutableStateOf(Color.White)
    }
    val linearGradient = Brush.linearGradient(
        colors = listOf(mainColor.value,  Color(0xFF8474ac)),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .background(brush = linearGradient)
    ) {}
        ProvideWindowInsets {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(rememberInsetsPaddingValues(insets = LocalWindowInsets.current.systemBars))
            )
            {
                TrackWaitingUi(
                    show = show,
                    mainColor,
                    currentView = currentView,
                    lastTrackModel = lastTrackModel,
                    mediaPlayerServiceConnection = mediaPlayerServiceConnection
                )
            }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackWaitingUi(show: MutableState<Boolean>,
                   backgroundColor: MutableState<Color>,
                   currentView: MutableState<ViewType>,
                   lastTrackModel: MusicTrackModel,
                   mediaPlayerServiceConnection : MediaPlayerServiceConnection,
                   ){
    val context = LocalContext.current
    val currentTrack = lastTrackModel.currentTrack.observeAsState()
    val lastId = lastTrackModel.lastTrackId.observeAsState()
    var layoutWidth by remember {
        mutableStateOf(0f)
    }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                layoutWidth = it.size.toSize().width
            }
        ,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        topBar(Modifier)
        var bitmap : Bitmap? by remember {
            mutableStateOf(null)
        }
        val pagerState = lastTrackModel.lastTrackId.value?.let {
            rememberPagerState(
                initialPage = it,
                initialPageOffsetFraction = 0f,
                pageCount = {
                    lastTrackModel.mp3fileList.size
                }
            )
        } ?: return@Column

        LaunchedEffect(lastId.value) {
            bitmap = currentTrack.value?.cover?.let { lastTrackModel.getBitmap(it) }
            val palette =
                bitmap?.let { Palette.from(it).generate() }
            val darkVibrantSwatch = palette?.darkVibrantSwatch
            backgroundColor.value = darkVibrantSwatch?.let { Color(it.rgb) }
                ?: Color.White
            launch {
                lastTrackModel.lastTrackId.value?.let { pagerState.scrollToPage(it) }
            }
        }

        var lastPage by remember {
            mutableIntStateOf(pagerState.currentPage)
        }
        val kekKey = (pagerState.currentPageOffsetFraction.absoluteValue > 0 && pagerState.currentPageOffsetFraction.absoluteValue<0.08f)
                    && lastPage!=pagerState.currentPage
        if (kekKey){
            LaunchedEffect(Unit) {
                //Log.d("page",currentPage.toString())
                lastPage = pagerState.currentPage
                pagerState.currentPage.let {
                    mediaPlayerServiceConnection.setIndex(it)
                    //lastTrackModel.currentTrack.value = lastTrackModel.mp3fileList[it]
                    mediaPlayerServiceConnection.play()
                    //lastTrackModel.currentSongPosition.value = 0L
                    //lastTrackModel.currentSongDuration.value = mediaPlayerServiceConnection.getExoInstance()?.duration
                }
            }
        }

        //}
        HorizontalPager(
            state = pagerState,
        ) {page->
            TrackCoverImg(page, show.value, lastTrackModel, bgColor = backgroundColor)
        }
        TitleAndArtist(show.value, lastTrackModel)
        Column {
            SongPositionDuration(lastTrackModel)
            TrackPositionSlider(lastTrackModel, mediaPlayerServiceConnection)
            playbackPanel(mediaPlayerServiceConnection, lastTrackModel, pagerState)
            Text("")
            Text("")
        }
    }
}

@Composable
fun SongPositionDuration(lastTrackModel: MusicTrackModel){
    val currentPosition = lastTrackModel.currentSongPosition.observeAsState()
    val currentDuration = lastTrackModel.currentSongDuration.observeAsState()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(formatDuration(currentPosition.value))
        Text(formatDuration(currentDuration.value))
    }

}


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipableTrackWaitingUi(
    show: MutableState<Boolean>,
    currentView: MutableState<ViewType>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection
) {
    var squareSize by remember { mutableStateOf(1f) }
    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { squareSize }
    val anchors = mapOf(0f to 0, -sizePx to -1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .onSizeChanged {
                squareSize = it.height.toFloat()
            }
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Vertical,
                velocityThreshold = 0.1.dp
            )
    ) {
        Box(
            Modifier
                .offset { IntOffset(0, 0) }
                .clip(RoundedCornerShape(16.dp))
                .graphicsLayer {
                    translationY = swipeableState.offset.value
                    if (translationY <= -sizePx || translationY >= sizePx) {
                        show.value = false
                    }
                }
        ) {
            TrackWaitingWrapper(
                show,
                currentView,
                lastTrackModel,
                mediaPlayerServiceConnection
            )
        }
    }
}

@Composable
fun TrackCoverImg(
    id:Int,
    show: Boolean,
    lastTrackModel: MusicTrackModel,
    bgColor: MutableState<Color>
){// backgroundColor:MutableState<Color>
    val context = LocalContext.current
    val currentTrack by remember {
        mutableStateOf(lastTrackModel.mp3fileList[id])
    }

    val painter = if (currentTrack.cover != null){
        rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(currentTrack.cover)
                .memoryCacheKey(currentTrack.cover)
                .allowHardware(false)
                .build(),
            lastTrackModel.imageLoader,
        )
    }else{
        painterResource(id = R.drawable.musicnote)
    }
    //TitleAndArtist(currentTrack, show)
    val borderWidth = 4.dp
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f), contentAlignment = Alignment.Center
    ) {
            Image(
                painter = painter,
                contentDescription = "cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(borderWidth)
                    .clip(CircleShape)
                    .size(300.dp)
            )
    }

}

@Composable
fun TitleAndArtist(show: Boolean, musicTrackModel: MusicTrackModel){
    val currentTrack by musicTrackModel.currentTrack.observeAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        currentTrack?.title?.let {
                val width = 200.dp
                val dateColumnWidth =
                    measureTextWidth(it.take(90), MaterialTheme.typography.headlineSmall)
                if (dateColumnWidth <= 200.dp) {
                    CustomText(
                        text = it,
                        Modifier,
                        fontSize = 20.sp
                    )
                } else {
                    MarqueeText(
                        1,
                        show,
                        false,
                        it.take(90),
                        Modifier.width(width),
                        fontSize = 20.sp
                    )
                }
            }
        }
        currentTrack?.artist?.let {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    it, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun playbackPanel(mediaPlayerServiceConnection : MediaPlayerServiceConnection,
                  lastTrackModel: MusicTrackModel,
                  pagerState:PagerState){
    val exoInstance = mediaPlayerServiceConnection.getExoInstance()
    var isPlaying by remember {
        mutableStateOf( exoInstance?.isPlaying ?: false)
    }

    val iconSize = 24.dp
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            mediaPlayerServiceConnection.playNextTrack(-1)
            coroutineScope.launch {
                // Call scroll to on pagerState
                lastTrackModel.lastTrackId.value?.let { pagerState.scrollToPage(it) }
            }
        }) {
            Image(
                painter = painterResource(R.drawable.fast_forward),
                contentDescription = "Fast Forward Icon",
                modifier = Modifier
                    // Use the flip modifier to flip the icon horizontally
                    .scale(-1f, 1f)
                    // Set other modifiers if needed, like size or padding
                    .size(iconSize),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
        val playButtonImage = if (isPlaying) {
            painterResource(R.drawable._11871_pause_icon)
        } else {
            painterResource(R.drawable.play_button)
        }
        Box(modifier = Modifier
            .size(iconSize * 4)
            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    if (!isPlaying) {
                        if (lastTrackModel.initialPosition == lastTrackModel.currentSongPosition.value) {
                            exoInstance?.seekTo(lastTrackModel.initialPosition)
                        }
                        exoInstance?.play()
                    } else {
                        exoInstance?.pause()
                    }
                    isPlaying = !isPlaying
                    lastTrackModel.isPlaying.value = !lastTrackModel.isPlaying.value!!
                }
            ) {
                Image(
                    painter = playButtonImage,
                    contentDescription = if (isPlaying) "Pause Track Icon" else "Play Track Icon",
                    modifier = Modifier.size(iconSize),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        IconButton(
            onClick = {
                mediaPlayerServiceConnection.playNextTrack(1)
                coroutineScope.launch {
                    // Call scroll to on pagerState
                    lastTrackModel.lastTrackId.value?.let { pagerState.scrollToPage(it) }
                }

            }
        ) {
            Image(
                painter = painterResource(R.drawable.fast_forward),
                contentDescription = "Forward Track Icon",
                modifier = Modifier
                    .size(iconSize),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }

    }
}
