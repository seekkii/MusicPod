package com.example.myapplication

import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class MusicPodSettings(settingsPref: SharedPreferences) : ViewModel() {
    private val prefs = settingsPref
    val currentSettings = MutableLiveData("Settings")
    private val prefTimerSetting = "timer_setting"
    private val prefSkipSilentSetting = "skip_silent"
    private val prefPlaybackSpeed = "playback_speed"
    private val prefTheme = "theme"
    private val prefLanguage = "language"
    private val prefTabList = "tab-list"

    var timer = getSettingInt(prefTimerSetting, 0)
    var skipSilenceBetweenTrack = getSettingBool(prefSkipSilentSetting, false)
    var playbackSpeed = getSettingInt(prefPlaybackSpeed, 1)
    var theme = getSettingInt(prefTheme, 0)
    var tabList = getList()
    var language = getSettingString(prefLanguage, "English")
    init {
        // Initialization logic
    }
    fun saveAllSetting() {
        saveSettingInt(prefTimerSetting, timer)
        saveSettingBoolean(prefSkipSilentSetting, skipSilenceBetweenTrack)
        saveSettingInt(prefPlaybackSpeed, playbackSpeed)
        saveSettingInt(prefTheme, theme)
        saveSettingString(prefLanguage, language)
        saveTabList(prefTabList, tabList)
    }

    fun timerToString():String{
        return when (timer){
            0->{
                "Off"
            }
            else->{
                "$timer minutes"
            }
        }
    }

    inline fun themeToString():String{
        return when (theme){
            0->{
                "Dark"
            }

            1->{
                "White"
            }

            else->{
                "Custom"
            }
        }
    }
    fun saveTabList(key:String, tabList: List<String>) : List<String> {
        val serializedList = Gson().toJson(tabList)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(key, serializedList)
        editor.apply()
        return tabList
    }

    private fun getList(): List<String> {
        val serializedList: String? = prefs.getString(prefTabList, "")
        if (serializedList.isNullOrEmpty()){
            return emptyList()
        }
        val trackType = object : TypeToken<List<String>>() {}.type
        val gson = GsonBuilder().create()
        return gson.fromJson(serializedList, trackType)
    }
    
    private fun saveSettingInt(preferencesName: String, content: Int) : Int {
        val editor = prefs.edit()
        editor.putInt(preferencesName, content)
        editor.apply()
        return content
    }
    private fun saveSettingString(preferencesName: String, content: String) : String {
        val editor = prefs.edit()
        editor.putString(preferencesName, content)
        editor.apply()
        return content
    }
    private fun saveSettingBoolean(preferencesName: String, content: Boolean) : Boolean {
        val editor = prefs.edit()
        editor.putBoolean(preferencesName, content)
        editor.apply()
        return content
    }

    private fun getSettingString(preferencesName: String, defaultValue: String): String {
        return prefs.getString(preferencesName, defaultValue) ?: defaultValue
    }

    private fun getSettingInt(preferencesName: String, defaultValue: Int): Int {
        return prefs.getInt(preferencesName, defaultValue)
    }

    private fun getSettingBool(preferencesName: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(preferencesName, defaultValue)
    }
}

@Composable
fun SettingsMainview(
    settingsViewModel: MusicPodSettings,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    onPlayBackNavigation : () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        state = rememberLazyListState(), // Pass the LazyListState to the LazyColumn
    ) {
        item {
            GetPremiumVersion()
        }
        item {
            PlaybackSetting(settingsViewModel, mediaPlayerServiceConnection) {
                onPlayBackNavigation()
            }
        }
        item {
            CustomizeSetting(settingsViewModel)
        }
        item {
            PermissionSetting(settingsViewModel)
        }

    }

    DisposableEffect(Unit){
        onDispose(){
            //settingsViewModel.saveAllSetting()
        }
    }
}

@Composable
fun SettingNavigation(
    musicPodSettings:MusicPodSettings,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    onNavigateOut : ()->Unit
)
{
    val textColor = MaterialTheme.colorScheme.background
    val textStyle = MaterialTheme.typography.titleLarge
    val navController = rememberNavController()

    ProvideWindowInsets {
        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .padding(rememberInsetsPaddingValues(insets = LocalWindowInsets.current.systemBars))
        ) {
            val (settingTitle, setting1, setting2, setting3) = createRefs()
            val currentSetting by musicPodSettings.currentSettings.observeAsState("Setting")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
                    //.height(40.dp)
                    .constrainAs(settingTitle) {
                        top.linkTo(parent.top)
                        bottom.linkTo(setting1.top)
                    },
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    {
                        if (musicPodSettings.currentSettings.value == "Timer"){
                            navController.navigate("settingMain")
                            musicPodSettings.currentSettings.value = "Setting"
                        }else{
                            onNavigateOut()
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Gray)
                }
                Text(
                    text = currentSetting,
                    color = textColor,
                    fontSize = textStyle.fontSize,
                    fontStyle = textStyle.fontStyle
                )
            }

            NavHost(
                navController = navController,
                modifier = Modifier.constrainAs(setting1) {
                    top.linkTo(settingTitle.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                },
                startDestination = "settingMain",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                composable("settingMain") {
                    SettingsMainview(
                        settingsViewModel = musicPodSettings,
                        mediaPlayerServiceConnection = mediaPlayerServiceConnection,
                        )
                    {navController.navigate("timer")}
                }

                composable("timer") {
                    SleepTimer(onNavigateToSongList = {  })
                }
            }
        }
    }
}

@Composable
fun PlaybackSetting(settings: MusicPodSettings,
                    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
                    onNavigateToTimerList: () -> Unit
){
    val textStyle = MaterialTheme.typography.titleLarge
    val textColor = MaterialTheme.colorScheme.background

    Text(
        text = "Playback",
        Modifier.padding(20.dp),
        color = textColor,
        fontStyle = textStyle.fontStyle,
        //fontSize = textStyle.fontSize
    )
    Column(
        Modifier
            .fillMaxWidth()
            //.background(Color.Black.copy(0.5f), shape = RoundedCornerShape(20.dp)),
    ) {
        SettingOptionDisplayTop(text = "Timer", settings.timerToString()){
            onNavigateToTimerList()
            settings.currentSettings.value = "Timer"
        }
        SettingOptionDisplayWithToggle(
            text = "Skip silence between tracks",
            settings = settings,
            mediaPlayerServiceConnection = mediaPlayerServiceConnection)
        SettingOptionDisplayBottom(text = "Playback speed", settings.playbackSpeed.toString()) {}
    }
}

@Composable
fun PermissionSetting(settings: MusicPodSettings){
    val textStyle = MaterialTheme.typography.titleLarge
    val textColor = MaterialTheme.colorScheme.background

    Text(
        text = "Customize",
        Modifier.padding(20.dp),
        color = textColor,
        fontStyle = textStyle.fontStyle,
        //fontSize = textStyle.fontSize
    )
    Column(
        Modifier
            .fillMaxWidth()
            //.background(Color.Black.copy(0.5f), shape = RoundedCornerShape(20.dp))
    ) {
        SettingOptionDisplayTop(text = "Theme") {}
        SettingOptionDisplayMiddle(text = "Text size") {}
        SettingOptionDisplayMiddle(text = "Dark mode") {}
        SettingOptionDisplayBottom(text = "Dark mode") {}

    }

}
@Composable
fun GetPremiumVersion(){

}
@Composable
fun CustomizeSetting(settings: MusicPodSettings){
    val textStyle = MaterialTheme.typography.titleLarge
    val textColor = MaterialTheme.colorScheme.background

    Text(
            text = "Customize",
            Modifier.padding(20.dp),
            color = textColor,
            fontStyle = textStyle.fontStyle,
            //fontSize = textStyle.fontSize
        )
    Column(
        Modifier
            .fillMaxWidth()
            //.background(Color.Black.copy(0.5f), shape = RoundedCornerShape(20.dp))
    ) {
        SettingOptionDisplayTop(
            text = "Theme",
            optionValue = settings.themeToString()) {
        }
        SettingOptionDisplayMiddle(text = "Text size", optionValue = textStyle.fontSize.value.toString()) {}
        SettingOptionDisplayMiddle(text = "Manage tabs") {}
        SettingOptionDisplayBottom(text = "Language", optionValue = settings.language) {}
    }
}

@Composable
fun SettingOptionDisplayTop(
    text: String,
    optionValue: String = "",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onNavigateToSetting :() -> Unit,
)
{
    val textPadding by remember { mutableStateOf(20.dp) }
    val fontTitleMedium = MaterialTheme.typography.titleMedium

    val textColor = MaterialTheme.colorScheme.background

    var isPressed by remember { mutableStateOf(false) }

    // Use rememberUpdatedState to ensure the animation restarts when isPressed changes
    val transition = updateTransition(targetState = isPressed, label = "buttonTransition")
    val animatedAlpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(durationMillis = 10000)
            } else {
                tween(durationMillis = 10000)
            }
        }, label = ""
    ) { state ->
        if (state) 0f else 0.5f
    }

    Box() {
        Column(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(20.dp, 20.dp))
                .background(
                    Color.Black.copy(animatedAlpha)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = Color.White.copy(0.5f))
                ) {
                    isPressed = true
                },
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = text,
                Modifier.padding(
                    //top = textPadding,
                    start = textPadding,
                    //bottom = 10.dp
                ),
                color = textColor,
                fontStyle = fontTitleMedium.fontStyle,
                fontSize = fontTitleMedium.fontSize
            )
            if (optionValue!="") {
                Text(
                    text = optionValue,
                    Modifier.padding(
                        start = textPadding,
                    ),
                    color = Color.White.copy(0.5f),
                    fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                    //fontSize = fontTitleSmall.fontSize
                )
            }
        }
    }

    // Observe the value of isPressed and trigger the action accordingly
   LaunchedEffect(isPressed) {
       if (isPressed) onNavigateToSetting()
       isPressed = false
    }
}

@Composable
fun SettingOptionDisplayMiddle(
    text: String,
    optionValue: String = "",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onNavigateToSetting :() -> Unit,
    )
{
    val textPadding by remember { mutableStateOf(20.dp) }
    val fontTitleMedium = MaterialTheme.typography.titleMedium

    val textColor = MaterialTheme.colorScheme.background

    val isPressed by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0f else 0.5f, label = "button animation"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Color.Black.copy(animatedAlpha)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(0.5f))
            ) {
                onNavigateToSetting()
            },
        )
    {
        HorizontalDivider(
            Modifier
                //.fillMaxSize()
                .padding(start = textPadding, end = textPadding),
            color = Color.White.copy(0.5f)
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = text,
                color = textColor,
                fontStyle = fontTitleMedium.fontStyle,
                fontSize = fontTitleMedium.fontSize
            )
            if (optionValue!="") {
                Text(
                    text = optionValue,
                    color = Color.White.copy(0.5f),
                    fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                    //fontSize = fontTitleSmall.fontSize
                )
            }
        }
    }
}

@Composable
fun SettingOptionDisplayWithToggle(
    text: String,
    settings: MusicPodSettings,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    optionValue: String = "",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
){
    val textPadding by remember { mutableStateOf(20.dp) }
    val fontTitleMedium = MaterialTheme.typography.titleMedium
    val textColor = MaterialTheme.colorScheme.background
    val isPressed by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0f else 0.5f, label = "button animation"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Color.Black.copy(animatedAlpha)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(0.5f))
            ) {
                // onToggle()
            },
    ) {
        HorizontalDivider(
            Modifier
                //.fillMaxSize()
                .padding(start = textPadding, end = textPadding),
            color = Color.White.copy(0.5f)
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            )
        {
            Column(
                //verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontStyle = fontTitleMedium.fontStyle,
                    fontSize = fontTitleMedium.fontSize
                )
                if (optionValue != "") {
                    Text(
                        text = optionValue,
                        color = Color.White.copy(0.5f),
                        fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                        //fontSize = fontTitleSmall.fontSize
                    )
                }
            }
            var checked by remember { mutableStateOf(settings.skipSilenceBetweenTrack) }
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    settings.skipSilenceBetweenTrack = checked
                    mediaPlayerServiceConnection.getExoInstance()?.skipSilenceEnabled = checked
                }
            )
        }
    }
}

@Composable
fun SettingOptionDisplayBottom(
    text: String,
    optionValue: String = "",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onNavigateToSetting :() -> Unit,
)
{
    val textPadding by remember { mutableStateOf(20.dp) }
    val fontTitleMedium = MaterialTheme.typography.titleMedium
    val textColor = MaterialTheme.colorScheme.background
    var isPressed by remember { mutableStateOf(false) }

    // Use rememberUpdatedState to ensure the animation restarts when isPressed changes
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0f else 0.5f, label = "button animation"
    )
    Box() {
        HorizontalDivider(
            Modifier
                //.fillMaxSize()
                .padding(start = textPadding, end = textPadding),
            color = Color.White
        )
        Column(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
                .background(
                    Color.Black.copy(animatedAlpha)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = Color.White.copy(0.5f))
                ) {
                    onNavigateToSetting()
                }
            ,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = text,
                Modifier.padding(
                    //top = textPadding,
                    start = textPadding,
                    //bottom = 10.dp
                ),
                color = textColor,
                fontStyle = fontTitleMedium.fontStyle,
                fontSize = fontTitleMedium.fontSize
            )
            if (optionValue!="") {
                Text(
                    text = optionValue,
                    Modifier.padding(
                        start = textPadding,
                    ),
                    color = Color.White.copy(0.5f),
                    fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                    //fontSize = fontTitleSmall.fontSize
                )
            }

        }
    }
}
@Composable
fun SleepTimer(onNavigateToSongList :() ->Unit)
{
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 2.dp, end = 2.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(0.5f))
            //.background(MaterialTheme.colorScheme.background)
    ) {
        textWithCheckBox(text = "Off")
        textWithCheckBox(text = "30 minutes")
        textWithCheckBox(text = "1 hour")
        textWithCheckBox(text = "1 hour 30 minutes")
        textWithCheckBox(text = "2 hours")
    }
}

@Composable
fun textWithCheckBox(text:String){
    var checkState by remember { mutableStateOf(false) }
    val fontStyle = MaterialTheme.typography.titleLarge
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(start = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleCheckbox(selected = checkState, modifier = Modifier) {
            checkState = !checkState
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.background,
            fontSize = fontStyle.fontSize
        )
    }
    HorizontalDivider(
        Modifier.padding(start = 60.dp, end = 20.dp)
        ,color = MaterialTheme.colorScheme.background.copy(0.5f)
    )
}

@Composable
fun AppNavigation(
    currentView: MutableState<ViewType>,
    uriImg: MutableState<Uri?>,
    mp3FilesList: MutableState<List<MusicTrack>>,
    lastTrackModel: MusicTrackModel,
    mediaPlayerServiceConnection: MediaPlayerServiceConnection,
    musicPodSettings: MusicPodSettings
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "landing",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("landing") {
           SettingNavigation(musicPodSettings, mediaPlayerServiceConnection){
               if (musicPodSettings.currentSettings.value == "Settings"){
                   navController.navigate("songList")
               }
           }
        }
        composable(
            "songList",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) { backStackEntry ->
           SongListView(
               currentView ,
               uriImg ,
               mp3FilesList  ,
               lastTrackModel  ,
               mediaPlayerServiceConnection
           ) {

           }
        }
    }
}
