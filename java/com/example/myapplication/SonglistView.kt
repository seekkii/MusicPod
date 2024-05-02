package com.example.myapplication

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import java.nio.ByteBuffer
import kotlin.math.absoluteValue

@Composable
fun RoundedIconButton(
    onClick: () -> Unit,
    text: String
) {
    TextButton(
        onClick = onClick,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Composable
fun ColorChip(color: Color, textCustomStyle: MutableState<TextStyle>){
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .size(30.dp),
        //.padding(4.dp),
        onClick = {
            textCustomStyle.value =
                TextStyle(
                    color = color
                )
        },
        content = {},
        color = color

    )
}
@Composable
fun TextCustomization(showTextCustomizeMenu: Boolean, textCustomStyle: MutableState<TextStyle>) {
    if (!showTextCustomizeMenu){
        return
    }

    val colorList = listOf(Color.Black, Color.White, Color.Blue, Color.Red, Color.Green)

    val initialSize = textCustomStyle.value.fontSize.value


    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(1f / 3f)
        .clickable { }
        .background(Color(0xFFF5F5F5))
    ){
        Row(

            //horizontalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colorList.forEach { color ->
                ColorChip(color = color, textCustomStyle)
            }
        }
        val initialFontSize = textCustomStyle.value.fontSize.value

        val fontSize = remember {
            mutableFloatStateOf(initialFontSize)
        }
        Slider(
            value = fontSize.floatValue,
            onValueChange = { newFontSize ->
                if (fontSize.floatValue == fontSize.floatValue.toInt()*1f){
                    textCustomStyle.value =
                        TextStyle(
                            fontSize = newFontSize.sp
                        )
                }

                fontSize.floatValue = newFontSize
                Log.d("fontsize", fontSize.floatValue.toString())
            },
            valueRange = initialFontSize..initialFontSize*3,

            modifier = Modifier
                .weight(1f)
        )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun pageIndicator(modifier: Modifier,
                  pagerState: PagerState,
                  transparency:MutableState<Float>)
{


    Box(modifier = Modifier
                //.background(Color.Blue)
                .graphicsLayer {
                    translationX = (pagerState.currentPage + pagerState.currentPageOffsetFraction).absoluteValue * size.width.absoluteValue
                    alpha = transparency.value
                },
    ){}

        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = 1 - transparency.value
                },
            horizontalArrangement = Arrangement.Center
        ) {
            if (pagerState.currentPage <= 1 || pagerState.currentPage in pagerState.pageCount - 2 until pagerState.pageCount) {
                val pageOffset = (
                        (pagerState.currentPage) + pagerState.currentPageOffsetFraction).absoluteValue
                // We animate the alpha, between 50% and 100%

                transparency.value = lerp(
                    start = 0f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }

            repeat(pagerState.pageCount - 2) { iteration ->
                val color = if (pagerState.currentPage == iteration + 1) Color.White else Color.Transparent
                val text = when (iteration) {
                    0 -> "Song"
                    1 -> "Playlist"
                    2 -> "Favorites"
                    else -> ""
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    EnlargeTextBoxOncick(
                        text,
                        if (pagerState.currentPage == iteration + 1) {color}
                        else{Color.White.copy(0.5f)},
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                //translationX = (offsetFraction) * size.width.absoluteValue / 3
                                translationX = (pagerState.currentPageOffsetFraction) * size.width
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                                .height(2.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListView(
    currentView: MutableState<ViewType>,
    uriImg:MutableState<Uri?>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    onNavigateToSetting: () -> Unit
) {
    val gradientColors = listOf(
        Color(0xFF800080), // Gradient start color deep purple
        //Color(0xFFF4C4F3),//pink
        //Color(0XFFFC67FF),//Sharp Pink
        //Color(0xFFFFC0CB), // Gradient stop color blue
        //Color(0xFFEF32D9),//light purple
        Color(0xFF8474ac),//purple
        //Color(0xFF89FFFD)//light blue
    )

    val context = LocalContext.current


    val showThemeCustom = remember { mutableStateOf(false) }
    val showTrackWaitUI = remember {
        mutableStateOf(false)
    }
    if (showThemeCustom.value) {
        currentView.value = ViewType.THEMECUSTOMIZATIONVIEW
    }
    /*BackHandler {
        showTrackWaitUI.value = false
    }

     */
    val offsetY = remember { mutableFloatStateOf(0f) }
    val transparency = remember {
        mutableFloatStateOf(1f)
    }
    val musicPlayerScreenHeight = remember {
        mutableFloatStateOf(1f)
    }
    val appBarHeight = remember {
        mutableFloatStateOf(1f)
    }

    ProvideWindowInsets {
        val insets = LocalWindowInsets.current.systemBars
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    rememberInsetsPaddingValues(insets = insets)
                )
        ) {
            val (middleView, utility) = createRefs()
            val showSearchBar = remember {
                mutableStateOf(false)
            }
            val pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0f,
                pageCount = {
                    5
                })
            Column(
                Modifier
                    .fillMaxSize()
                    //.nestedScroll()
                    .constrainAs(middleView) {
                        top.linkTo(parent.top)
                        bottom.linkTo(utility.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    },
            )
            {
                TopMenuBar(
                    currentView,
                    uriImg,
                    offsetY,
                    onNavigateToSetting
                )

                    pageIndicator(
                        Modifier,//.offset { IntOffset(0, offsetY.value.toInt())},
                        pagerState,
                        transparency
                    )

                    MiddleContent(
                        currentView,
                        mp3FilesList,
                        uriImg,
                        lastTrackModel,
                        mediaPlayerServiceConnection,
                        showSearchBar,
                        showTrackWaitUI,
                        pagerState
                    )
            }

            val bottomUltility = Modifier.constrainAs(utility) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .alpha(1 - transparency.floatValue)
                    .then(bottomUltility)
            ) {
                MusicPlayerScreen2(
                    transparency,
                    showTrackWaitUI,
                    lastTrackModel  ,
                    mediaPlayerServiceConnection
                )
                PlayListUltility(modifier = bottomUltility, musicTrackModel = lastTrackModel)
            }
        }
        /*
        MusicPlayerScreen(
            showTrackWaitUI,
            lastTrackModel,
            mediaPlayerServiceConnection,
            Modifier
                .padding(top = 100.dp)
                .alpha(transparency.floatValue)
        )

         */

    }

    if (showTrackWaitUI.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            SwipableTrackWaitingUi(
                show = showTrackWaitUI,
                currentView = currentView,
                lastTrackModel = lastTrackModel,
                mediaPlayerServiceConnection = mediaPlayerServiceConnection
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMenuBar(
    currentView: MutableState<ViewType>,
    utiImg:MutableState<Uri?>,
    appbarHeight : MutableState<Float>,
    onNavigateToSetting : () ->Unit
){
    val context = LocalContext.current

    val showSearchBar = remember {
        mutableStateOf(false)
    }
    val showThemeCustom = remember{ mutableStateOf(false) }
    if (showThemeCustom.value){
        currentView.value= ViewType.THEMECUSTOMIZATIONVIEW
    }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "MusicPod")
            }
        },
        actions = {
            val showDropdownMenu = remember { mutableStateOf(false) }
            val showThemeDropdownMenu = remember { mutableStateOf(false) }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { newUri: Uri? ->
                if (newUri == null) return@rememberLauncherForActivityResult

                val input = context.contentResolver.openInputStream(newUri)
                    ?: return@rememberLauncherForActivityResult
                val outputFile = context.filesDir.resolve("newbg.png")
                input.use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                utiImg.value = outputFile.toUri()
                showThemeCustom.value = true
            }

            RoundedIconButton(onClick = {/*TODO*/ }, text = "Premium")
            IconButton(onClick = { showSearchBar.value = !showSearchBar.value }) {
                Icon(Icons.Filled.Search, null, tint = Color.Gray)
            }

            IconButton(onClick = {
                showDropdownMenu.value = !showDropdownMenu.value
            }) {
                Icon(Icons.Filled.Settings, "settingIcon")
                if (showDropdownMenu.value) {
                    DropdownMenu(
                        expanded = showDropdownMenu.value,
                        onDismissRequest = { showDropdownMenu.value = false },
                        ) {
                        DropdownMenuItem({Text("Theme")},onClick = {
                            showThemeDropdownMenu.value = true
                        })
                        DropdownMenuItem({Text("Setting")},onClick = onNavigateToSetting)
                    }
                }

                if (showThemeDropdownMenu.value) {
                    DropdownMenu(
                        expanded = showThemeDropdownMenu.value,
                        onDismissRequest = {
                            showThemeDropdownMenu.value = false
                            showDropdownMenu.value = false
                            //showTextCustomizeMenu = false
                        },
                        modifier = Modifier.animateContentSize()
                    ) {
                        DropdownMenuItem({Text("Set Theme")},onClick = {
                            launcher.launch("image/*")
                        })
                        DropdownMenuItem({Text("CALL HELP")},onClick = { /* Handle theme change */ })

                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
            .onGloballyPositioned {
                appbarHeight.value = it.size.height.toFloat()
            }
            //.graphicsLayer {
            //translationY = offsetY.value //* size.height/musicPlayerScreenHeight.value
            //alpha = 1f - (translationY / size.height).absoluteValue.coerceIn(0f, 1f)
       // }
        ,
            //.offset { IntOffset(0, (offsetY.value).toInt()) },
        windowInsets = WindowInsets(
            LocalWindowInsets.current.systemBars.left,
            0,
            LocalWindowInsets.current.systemBars.right,
            0
        )
    )
}

@Composable
fun PlayListUltility(modifier: Modifier, musicTrackModel: MusicTrackModel){
    val showPlaylistUltility = musicTrackModel.playlistModel.showPlaylistUltility.observeAsState()
    if (showPlaylistUltility.value == false){
        Box(modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
        ){
        }
        return
    }

    BackHandler {
        musicTrackModel.playlistModel.showPlaylistUltility.value = false
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black.copy(alpha = 0.5f))
        .then(modifier)){
        val buttonModifier = Modifier.weight(1f)
        ViewUltilityItem(
            text = "Play",
            imageVector = Icons.Filled.PlayArrow,
            modifier = buttonModifier,
            onClick = {}
        )

        ViewUltilityItem(
            text = "Add",
            imageVector = Icons.Default.Add,
            modifier = buttonModifier,
            onClick = {}
        )
        ViewUltilityItem(
            text = "Delete",
            imageVector = Icons.Default.Delete,
            modifier = buttonModifier,
            onClick = {
                musicTrackModel.playlistModel.deletePlaylist()
            }
        )

        ViewUltilityItem(
            text = "Rename",
            imageVector = Icons.Default.Edit,
            modifier = buttonModifier,
            onClick = {}
        )
    }
}
@Composable
fun MainviewBottom(modifier: Modifier)
{
    Row(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
        )

    )
    {
        val buttonModifier = Modifier.weight(1f)
        ViewOptionItem(
            text = "Home",
            photo = R.drawable.musicnote,
            modifier = buttonModifier
        )

        ViewOptionItem(
            text = "Your music",
            photo = R.drawable.musicnote,
            modifier = buttonModifier
        )
        ViewOptionItem(
            text = "Soundcloud",
            photo = R.drawable.musicnote,
            modifier = buttonModifier
        )


    }
}


@Composable
fun ViewUltilityItem(
    text:String,
    imageVector: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = onClick
            )
            .padding(6.dp)
            .then(modifier)
    ){
        Icon(imageVector, null, tint = Color.Gray,
            modifier = Modifier.size(30.dp))
        Text(
            text = text,
            modifier = Modifier
                .padding(start = 8.dp)
        )
    }
}

@Composable fun ViewOptionItem(text: String,photo:Int, modifier: Modifier){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = {
                    when (text) {
                        "Home" -> {
                            //currentView.value = ViewType.HOME
                        }

                        "Your music" -> {
                            //currentView.value = ViewType.SONGLISTVIEW
                        }

                        "Soundcloud" -> {
                            //currentView.value = ViewType.POPUP
                        }

                    }
                }
            )
            .padding(6.dp)
            .then(modifier)
    ){
        Image(
            painter = painterResource(photo),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(start = 8.dp)
        )
    }
}

@Composable fun ViewMiniOptionItem(text: String, currentView: MutableState<ViewType>,photo:Int, modifier: Modifier){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = {
                    when (text) {
                        "Playlist" -> {
                            currentView.value = ViewType.NAVIGATION
                        }

                        "Soundcloud" -> {
                            currentView.value = ViewType.ANALYZE
                        }

                    }
                }
            )
            .then(modifier)
    ){
        Text(
            text = text,
            modifier = Modifier
                .padding(start = 8.dp),
            color = Color.White

        )
    }
}


