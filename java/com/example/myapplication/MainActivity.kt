package com.example.myapplication

//composed


import android.Manifest.permission.FOREGROUND_SERVICE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.Timeline
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.nio.ByteBuffer


private external fun imgSharpenCPP(
    byteBuffer: ByteBuffer?,
    newBuffer:ByteBuffer?,
    width: Int,
    height: Int,
)
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Scanner object to scan for audio files~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
val start = System.currentTimeMillis()

class MusicTrack(
    var id: Int,
    var retrieveId: Int,
    val title: String?,
    val artist: String?,
    val album: String?,
    val cover: String? = null,
    val mp3Uri : String? = null,
    var fav: Boolean = false,

){
    // Adding additional properties and methods here if needed.
}

object Mp3Scanner {
    val supportedFileExtensions = listOf(".mp3")

    fun scanForMp3Files(context: Context, mp3fileList: List<MusicTrack> = emptyList()): List<MusicTrack> {
        val mp3FilesList: MutableList<MusicTrack> = mutableListOf()

        val selection = StringBuilder("title != '' AND ")
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val internalContentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val uris = listOf(externalContentUri, internalContentUri)

        val contentResolver: ContentResolver = context.contentResolver

        // Add selection criteria for each directory
        val directories = listOf(
           // "Alarms", "Audiobooks",
            "Music", //"Notifications",
           // "Podcasts", "Ringtones," ,"Movies",/* "Recordings",*/
            "Download" // Include the "Download" folder
        )
        val directorySelection = directories.joinToString(" OR ") {
            "${MediaStore.Audio.Media.DATA} LIKE '%/$it/%'"
        }
        // Combine the overall selection query
        selection.append("($directorySelection)")// AND ${MediaStore.Audio.Media.MIME_TYPE} = 'audio/mpeg'")
        // Query the media store for audio files
        var index = 0
        val metadataRetriever = MediaMetadataRetriever()
        for (uri in uris) {
            val cursor: Cursor? = contentResolver.query(
                uri,
                projection,
                selection.toString(),
                null,
                null
            )
            cursor?.use { it ->
                try {
                    while (it.moveToNext()) {
                        // Get the file path from the cursor
                        val filePath =
                            it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val fileId: Long =
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val fileUri: Uri = Uri.fromFile(File(filePath))
                        val displayName: String =
                            it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                        val dateAdded : String = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))

                       // if (!filePath.endsWith(".mp3", true)) { // Case-insensitive check
                         //   break
                        //}
                        //Log.d("filepath", displayName)
                        // Set the data source to the file path obtained from the cursor
                        metadataRetriever.setDataSource(filePath)
                        // Retrieve metadata information for the current track
                        var title =
                            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        var artist =
                            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        var album =
                            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)


                        if (title == null) {
                            title = displayName.substring(0, displayName.lastIndexOf("."))
                        }
                        if (artist == null) {
                            artist = "Unknown"
                        }
                        if (album == null) {
                            album = ""
                        }
                        val albumArt = metadataRetriever.embeddedPicture
                        val coverUri = if (albumArt != null) {
                            // Save the album art data to internal storage
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            val originalBitmap =
                                BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)

                            val resizedBitmap =
                                Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)

                            val file = File(context.filesDir, "album_art_$fileId.jpg")
                            val outputStream = FileOutputStream(file)

                            // val ogImgBuffer = ByteBuffer.wrap(albumArt);
                            // val sharpenedImg = ByteBuffer.wrap(albumArt);
                            // imgSharpenCPP(ogImgBuffer, sharpenedImg, width, height)
                            outputStream.use {
                                it.write(albumArt)
                            }

                            // Get the Uri for the saved file using the FileProvider
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            ).toString()
                        }
                        else {
                            null
                        }
                        val track =
                            MusicTrack(index, index, title, artist, album, coverUri, fileUri.toString())
                        mp3FilesList.add(track)
                        index++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cursor.close()
                    metadataRetriever.release()
                }
            }
        }


        return mp3FilesList
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Persistence Storage~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


class MusicTrackModel(context: Context, prefs: SharedPreferences) : ViewModel() {
    val playlistModel = PlaylistModel()
    val imageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()
    var sortOrder = getSortOrder(prefs)
    var lastTrackId = MutableLiveData<Int?>()
    var currentSongPosition = MutableLiveData<Long>()
    var currentSongDuration = MutableLiveData<Long>()
    var currentTrack = MutableLiveData<MusicTrack?>()
    var mp3fileList : List<MusicTrack> = emptyList()
    private var cacheBitmapMap : MutableMap<Int?, Pair<Long, Bitmap>> = mutableMapOf()
    var initialPosition = 0L
    //var initialDuration = 0L
    var isPlaying = MutableLiveData(false)

    init {
        val curId = getCurrentPlayingIndex(prefs)
        lastTrackId.value = curId
        currentTrack.value = MusicTrack(curId,curId,"","","")
        val currentSongPos = getCurrentSongPosition(prefs)
        val currentSongDur = getCurrentSongDuration(prefs)
        //currentSongDuration.value = currentSongDur.takeIf { it > 0 } ?: 0L
        currentSongPosition.value = currentSongPos.takeIf { it <= currentSongDur } ?: 0L
        initialPosition = currentSongPosition.value!!
        //initialDuration = currentSongDuration.value!!
    }

    fun getBitmap(key: String): Bitmap? {
        val memoryCacheKey = MemoryCache.Key(key)
        return imageLoader.memoryCache?.get(memoryCacheKey)?.bitmap

        /*
        memoryCacheKey?.forEach {
            if (it.key == key) {
                bitmap =
            }
        }

        val pair = cacheBitmapMap[key]
        if (pair == null)
            return null
        else {
            val timestamp = pair.first
            val currentTime = System.currentTimeMillis()
            val expirationPeriod = 3600000 // 1 hour in milliseconds

            return if (currentTime - timestamp <= expirationPeriod) {
                pair.second
            } else {
                val currentBitmap = pair.second
                // The cached item has expired, remove it
                cacheBitmapMap.remove(key)
                currentBitmap
            }
        }

         */
    }

    fun sortMp3FileList(sortKey:String):List<MusicTrack>{
        when (sortKey) {
            "Title" -> {
                mp3fileList = mp3fileList.sortedBy { it.title }
            }
            "Artist" -> {
                mp3fileList = mp3fileList.sortedBy { it.artist }
            }
            "Last added" -> {
                mp3fileList = mp3fileList.sortedBy { it.retrieveId }
            }
            else ->{}
        }
        return  mp3fileList
    }

    fun deleteFile(context: Context, uri: Uri) {
        var filePath: String? = null
        // Check if the URI is a content URI
        if ("content" == uri.scheme) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val contentResolver: ContentResolver = context.contentResolver

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = cursor.getString(columnIndex)
                }
            }
        }
        // If the URI is a file URI
        else if ("file" == uri.scheme) {
            filePath = uri.path
        }

        val file = filePath?.let { File(it) }

        if (file?.exists() == true) {
            val deleted = file.delete()

            if (deleted) {
                // File deleted successfully
                // Handle success or perform any additional tasks
            } else {
                // Failed to delete file
                // Handle failure or perform any necessary actions
            }
        } else {
            // File doesn't exist
            // Handle accordingly
        }
    }
    fun getSong(text: String): List<MusicTrack> {
         return mp3fileList.filter { it.title?.contains(text,ignoreCase = true)?:false }
    }
}

class PlaylistModel : ViewModel() {
    var playListMap : MutableMap<String,MutableList<Int>> = mutableMapOf()
    var playlistDeleteList: MutableList<String> = mutableListOf()
    var modified :Boolean = false
    var currentPlaylistName = ""
    var selectedAmountOfSong = MutableLiveData(0)
    var showPlaylistUltility = MutableLiveData(false)
    var checkAll = MutableLiveData(false)

    fun playlistToMp3List(mp3fileList: List<MusicTrack>): List<MusicTrack> {
        val currentPlaylist = playListMap[currentPlaylistName]
        return currentPlaylist?.zip(mp3fileList)
            ?.map { (playlistIndex, _) -> mp3fileList[playlistIndex] } ?: emptyList()
    }

    fun getPlaylistList():MutableList<Int>{
        return playListMap[currentPlaylistName] ?: mutableListOf()
    }

    fun deletePlaylist(){
        for (item in playlistDeleteList){
            playListMap.remove(item)
        }
        modified = true
        showPlaylistUltility.value = false
        playlistDeleteList = mutableListOf()
    }
}

const val PREFS_NAME = "TRACK_PREFERENCE"
private const val PREF_CURRENT_PLAYING_INDEX = "current_playing_index"
private const val PREF_CURRENT_SONG_POSITION = "current_song_position"
private const val PREF_CURRENT_SONG_DURATION = "current_song_duration"
private const val PREF_SORTED_ORDER = "current_sort_order"

fun saveSortOrder(prefs: SharedPreferences,order:String){
    val editor = prefs.edit()
    editor.putString(PREF_SORTED_ORDER, order)
    editor.apply()
}
fun getSortOrder(prefs: SharedPreferences):String{
    return prefs.getString(PREF_SORTED_ORDER, "") ?:""
}

fun saveCurrentPlayingIndex(prefs: SharedPreferences, index: Int) {
    val editor = prefs.edit()
    editor.putInt(PREF_CURRENT_PLAYING_INDEX, index)
    editor.apply()
}
fun saveCurrentSongDuration(prefs: SharedPreferences, duration: Long) {
    val editor = prefs.edit()
    editor.putLong(PREF_CURRENT_SONG_DURATION, duration)
    editor.apply()
}
fun getCurrentSongDuration(prefs: SharedPreferences): Long {
    return prefs.getLong(PREF_CURRENT_SONG_DURATION, 0L) // 0 is the default value if the preference is not found
}
fun getCurrentSongPosition(prefs: SharedPreferences): Long {
    return prefs.getLong(PREF_CURRENT_SONG_POSITION, 0L) // 0 is the default value if the preference is not found
}
fun saveCurrentSongPosition(prefs: SharedPreferences, position: Long) {
    val editor = prefs.edit()
    editor.putLong(PREF_CURRENT_SONG_POSITION, position)
    editor.apply()
}
fun getCurrentPlayingIndex(prefs: SharedPreferences): Int {
    return prefs.getInt(PREF_CURRENT_PLAYING_INDEX, 0) // 0 is the default value if the preference is not found
}

inline fun <reified T> getScannedSongsList(json: String, typeToken: Type): T {
    val gson = GsonBuilder().create()
    return gson.fromJson<T>(json, typeToken)
}

inline fun saveScannedSongsAsList(prefs: SharedPreferences,key:String, mp3fileList: List<MusicTrack>) {
    val serializedList = Gson().toJson(mp3fileList)
    val editor: SharedPreferences.Editor = prefs.edit()
    editor.putString(key, serializedList)
    editor.apply()
}
inline fun serializePlaylist(prefs: SharedPreferences,key: String, playListMap : MutableMap<String,MutableList<Int>>){
    val serializedList = Gson().toJson(playListMap)
    val editor: SharedPreferences.Editor = prefs.edit()
    editor.putString(key, serializedList)
    editor.apply()
}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Passing exoPlayerHolder to start service~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class MediaPlayerServiceBinder(private val service: MediaPlayerService) : Binder() {
    // Method to get the ExoPlayer instance from the service
    fun setupExoPlayer(musicTrackModel: MusicTrackModel,settings: MusicPodSettings)  {
        synchronized(this) {
            service.setupExoplayer(musicTrackModel,settings)
        }
    }

    fun getExoInstance(): ExoPlayer{
        return synchronized((this)){
            service.getExoInstance()
        }
    }

    fun getCurrentPositionJob() : Job? {
        return synchronized(this) {
            service.getCurrentPositionJob()
        }
    }

    fun launchPositionJob() {
        synchronized(this) {
            service.launchPositionJob()
        }
    }

    fun setIndex(id :Int){
        synchronized(this) {
            service.setTrackIndex(id)
        }

    }
    fun play(){
        synchronized(this) {
            service.play()
        }
    }
    fun playNextTrack(skip: Byte){
        synchronized(this) {
            service.playNextTrack(skip)
        }
    }
    fun updatemp3FilesList(newList: List<MusicTrack>) {
        synchronized(this) {
            service.mp3FilesList = newList
        }
    }


}
// Custom ServiceConnection class to manage the service binding
class MediaPlayerServiceConnection(private val prefs: SharedPreferences,
                                   val musicTrackModel: MusicTrackModel,
                                   val settings: MusicPodSettings
)
    : ServiceConnection {
    private var binder: MediaPlayerServiceBinder? = null
    fun setIndex(id :Int) = binder?.setIndex(id)
    fun play() = binder?.play()
    fun playNextTrack(skip: Byte) = binder?.playNextTrack(skip)
    fun updatemp3FilesList(mp3List: List< MusicTrack>) = mp3List.let { binder?.updatemp3FilesList(it) }
    fun getExoInstance() = binder?.getExoInstance()
    fun getCurrentPositionJob() = binder?.getCurrentPositionJob()
    fun launchPositionJob() = binder?.launchPositionJob()

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = service as? MediaPlayerServiceBinder
        binder?.setupExoPlayer(musicTrackModel,settings)
        val currentId = getCurrentPlayingIndex(prefs)
        binder?.setIndex(currentId)
        binder?.play()
        val end = System.currentTimeMillis()
        d("aaa",(start-end).toString())
    }
    override fun onServiceDisconnected(name: ComponentName?) {
        binder = null
    }

    fun getTrackModel():MusicTrackModel{
        return musicTrackModel
    }
}


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaPlayerService : Service() {
    private val binder = MediaPlayerServiceBinder(this)

    private lateinit var exoPlayer:ExoPlayer
    var currentTrackIndex = 0
    private var currentPositionJob: Job? = null
    private lateinit var lastTrackModel:MusicTrackModel

    // Modify this based on your mp3FilesList
    var mp3FilesList: List<MusicTrack> = emptyList()
    fun setupExoplayer(trackModel:MusicTrackModel, settings: MusicPodSettings){
        synchronized(this) {
            lastTrackModel = trackModel
            mp3FilesList = lastTrackModel.mp3fileList

            exoPlayer = ExoPlayer.Builder(this)
                .setSkipSilenceEnabled(settings.skipSilenceBetweenTrack)
                .build()
            exoPlayer.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                      playNextTrack(1)
                    }
                    //Log.d("aa",lastTrackModel.currentSongDuration.value.toString())
                    launchPositionJob()
                }

                override fun onTimelineChanged(
                    timeline: com.google.android.exoplayer2.Timeline,
                    reason: Int
                ) {
                    // This method is called when the player's timeline changes
                    // You can check for the duration here
                    val durationMs = exoPlayer.duration
                    if (durationMs != C.TIME_UNSET) {
                        // The duration is available, you can use it
                        lastTrackModel.currentSongDuration.value = durationMs
                    } else {
                        // The duration is not yet available
                        Log.d("ExoPlayer", "Duration: Not available yet")
                    }
                }

            })
        }
    }

    fun getCurrentPositionJob() : Job? {
        return synchronized(this){
            currentPositionJob
        }
    }
    fun playNextTrack(skip:Byte){
        if (skip > 0) {
            // Move to the next track
            currentTrackIndex = (currentTrackIndex + skip) % mp3FilesList.size
        } else if (skip < 0) {
            // Handle the case where skip is less than 0
            currentTrackIndex--
            if (currentTrackIndex < 0) {
                currentTrackIndex = mp3FilesList.size - 1
            }
        }
        // Retrieve the URI of the next track
        if (currentTrackIndex > mp3FilesList.size || currentTrackIndex<0){
            return
        }
        // Play the next track
        lastTrackModel.currentSongPosition.value = 0L
        if (currentTrackIndex != mp3FilesList.size) {
            play()
            launchPositionJob()
        }
        else{
            exoPlayer.pause()
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun launchPositionJob() {
        if (MainActivity.isAppInForeground) {
            currentPositionJob?.cancel() // Cancel any existing job before starting a new one
            currentPositionJob = GlobalScope.launch(Dispatchers.Main) {
                //lastTrackModel.currentSongDuration.value = exoPlayer.duration
                while (isActive && exoPlayer.isPlaying) {
                    // Update UI elements with the current position (e.g., progress bar, text view)
                    lastTrackModel.currentSongPosition.value = exoPlayer.currentPosition
                    //Log.d("job","running")
                    delay(1000)
                }
            }
        }else {
            currentPositionJob?.cancel()
        }
    }


    fun getExoInstance():ExoPlayer{
        return synchronized(this){
            exoPlayer
        }
    }


    fun setTrackIndex(index: Int){
        synchronized(this) {
            currentTrackIndex = index
        }
    }

    fun play(){
        //synchronized(this) {
            if (mp3FilesList.isEmpty()) {
                d("mp3file", "No mp3 file found")
                stopSelf() // Stop the service if there are no files to play
                return
            }
            val uri = mp3FilesList[currentTrackIndex]
            if (uri.mp3Uri != null) {
                val mediaItem = MediaItem.fromUri(uri.mp3Uri.toUri())
                //audioProcessing.OpenClicked(mp3FilesList[currentTrackIndex].second)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                //val param = PlaybackParameters(0.5F)
                //exoPlayer.setPlaybackParameters(param)
                lastTrackModel.currentTrack.value = uri
                lastTrackModel.lastTrackId.value = currentTrackIndex
                lastTrackModel.currentTrack.value = mp3FilesList[currentTrackIndex]

                //lastTrackModel.currentSongDuration.value = exoPlayer.duration

                if (mediaItem.mediaId.isEmpty()) {
                    // Handle the case when the URI of the media item is empty or null
                    stopSelf() // Stop the service if the URI is invalid
                    return
                }

            }
        //}
    }

    override fun onCreate() {
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEDIA_CHANNEL_ID,
                "Media Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    /*private fun createPlayerNotificationManager(): PlayerNotificationManager {
        // Customize notification behavior and appearance here if needed
        val notificationListener = object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    // If the notification is ongoing, start the foreground service and show the notification
                    startForeground(notificationId, notification)
                } else {
                    // If the notification is not ongoing, just update the notification without calling stopForeground
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                stopSelf()
            }
        }

        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
                // Return the title of the currently playing track
                return "Track Title"
            }

            override fun createCurrentContentIntent(player: androidx.media3.common.Player): PendingIntent? {
                // Return a PendingIntent to the activity you want to open when the notification is clicked
                val intent = Intent(this@MediaPlayerService, MainActivity::class.java)
                return PendingIntent.getActivity(
                    this@MediaPlayerService,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence {
                // Return additional text for the notification, e.g., artist name, album name, etc.
                return "Artist Name - Album Name"
            }

            override fun getCurrentLargeIcon(
                player: androidx.media3.common.Player,
                callback: PlayerNotificationManager.BitListCallback
            ): BitList? {
                return BitListFactory.decodeResource(resources, R.drawable.musicnote)
            }
        }

        return PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            MEDIA_CHANNEL_ID,
            mediaDescriptionAdapter
        ).setNotificationListener(notificationListener)
            .build().apply {
                setSmallIcon(R.drawable.musicnote)
            }
    }

     */

    private fun buildNotification(): Notification {
        // Build the initial notification to show when the service is started as a foreground service
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
            .setContentTitle("Media Playback")
            .setContentText("Playing...")
            .setSmallIcon(R.drawable.musicnote)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return notificationBuilder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val MEDIA_CHANNEL_ID = "media_channel"
    }
}

class MainActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks {

    private var storge_permissions = arrayOf(
        READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storge_permissions_33 = arrayOf(
        READ_MEDIA_IMAGES,
        READ_MEDIA_AUDIO,
        READ_MEDIA_VIDEO,
        FOREGROUND_SERVICE
    )

    private fun permissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storge_permissions_33
        } else {
            storge_permissions
        }
        return p
    }
    private lateinit var mediaPlayerServiceConnection : MediaPlayerServiceConnection

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add this:
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("my_preference", MODE_PRIVATE)
        val imgPreferences = this.getSharedPreferences("img_preference", MODE_PRIVATE)
        val settingsPref = this.getSharedPreferences("setting_preference",MODE_PRIVATE)
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )
        application.registerActivityLifecycleCallbacks(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val mediaPlayerServiceIntent = Intent(this, MediaPlayerService::class.java)

        this.setContent {
            val context = LocalContext.current

            val imageViewModel = remember { ImageViewModel(imgPreferences) }
            val uriImg = remember {
                mutableStateOf(imageViewModel.selectedImageUri.value)
            }

            val lastTrackModel by remember{
                mutableStateOf( MusicTrackModel(context , sharedPreferences))}

            val isServiceStarted = remember { mutableStateOf(false) }
            val mp3FilesList = remember { mutableStateOf(emptyList<MusicTrack>()) }
            val settingsViewModel by remember {
                mutableStateOf(MusicPodSettings(settingsPref))
            }
            val mediaPlayerServiceConnection by remember {
                mutableStateOf(
                    MediaPlayerServiceConnection(
                        sharedPreferences,
                        lastTrackModel,
                        settingsViewModel
                    )
                )
            }

            val currentView = remember { mutableStateOf(ViewType.NAVIGATION) }
            val fontStyle = MaterialTheme.typography.bodyMedium
            val customTextStyle = remember{
                mutableStateOf(
                    fontStyle
                )
            }
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(uriImg.value)
                        //.memoryCacheKey(path)
                        .allowHardware(true)
                        .build(),
                    lastTrackModel.imageLoader,
                ),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            ViewChange(
                currentView,
                uriImg,
                mp3FilesList,
                lastTrackModel,
                imageViewModel,
                mediaPlayerServiceConnection,
                settingsViewModel
            )
            LaunchedEffect(Unit) {
                if (!isServiceStarted.value) {
                    permissions().let {
                        ActivityCompat.requestPermissions(this@MainActivity, it, 1)
                    }
                    // Use a coroutine to scan for mp3 files in the background
                    launch(Dispatchers.IO) {
                        val serializedList: String? = sharedPreferences.getString("AllSongs", "")
                        val trackType = object : TypeToken<List<MusicTrack>>() {}.type
                        val scannedMp3List: List<MusicTrack> =
                            if (serializedList.isNullOrBlank() || serializedList == "[]") {
                                val mp3List = Mp3Scanner.scanForMp3Files(context)
                                mp3List
                            } else {
                                getScannedSongsList<List<MusicTrack>>(serializedList, trackType)
                            }

                        mp3FilesList.value = scannedMp3List
                        context.startForegroundService(mediaPlayerServiceIntent)
                        bindService(
                            mediaPlayerServiceIntent,
                            mediaPlayerServiceConnection,
                            BIND_AUTO_CREATE
                        )

                        launch {
                            val serializedPlaylist: String? = sharedPreferences.getString("Playlist", "")
                            val playlistType =
                                object : TypeToken<Map<String, MutableList<Int>>>() {}.type
                            val playlistMap: MutableMap<String, MutableList<Int>> =
                                serializedPlaylist?.let { getScannedSongsList(it, playlistType) }
                                    ?: mutableMapOf()
                            lastTrackModel.playlistModel.playListMap = playlistMap
                        }
                        // Update the mp3FilesList with the scanned data
                        withContext(Dispatchers.Main) {
                            lastTrackModel.mp3fileList = scannedMp3List
                            this@MainActivity.mediaPlayerServiceConnection = mediaPlayerServiceConnection
                            mediaPlayerServiceConnection.getExoInstance()?.skipSilenceEnabled =
                                settingsViewModel.skipSilenceBetweenTrack
                        }
                        isServiceStarted.value = true
                    }
                }
            }

            DisposableEffect(mediaPlayerServiceConnection) {
                onDispose {
                    unbindService(mediaPlayerServiceConnection)
                    lastTrackModel.currentSongDuration.value?.let {
                        saveCurrentSongDuration(
                            sharedPreferences,
                            mediaPlayerServiceConnection.getExoInstance()?.duration
                                ?: getCurrentSongDuration(sharedPreferences)
                        )
                    }
                    lastTrackModel.currentSongPosition.value?.let {
                        saveCurrentSongPosition(
                            sharedPreferences,
                            mediaPlayerServiceConnection.getExoInstance()?.currentPosition
                                ?: getCurrentSongDuration(sharedPreferences)
                        )
                    }
                        lastTrackModel.lastTrackId.value?.let { it ->
                            saveCurrentPlayingIndex(
                                sharedPreferences,
                                it
                            )
                        }

                    saveScannedSongsAsList(sharedPreferences,"AllSongs", mp3FilesList.value)
                    saveSortOrder(sharedPreferences, lastTrackModel.sortOrder)
                    if (lastTrackModel.playlistModel.modified){
                        serializePlaylist(sharedPreferences,"Playlist",lastTrackModel.playlistModel.playListMap)
                    }
                    settingsViewModel.saveAllSetting()
                }
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        isAppInForeground = true
        if (::mediaPlayerServiceConnection.isInitialized) {
            mediaPlayerServiceConnection.launchPositionJob()
        }

    }

    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
        if (::mediaPlayerServiceConnection.isInitialized) {
            mediaPlayerServiceConnection.getCurrentPositionJob()?.cancel()
        }
    }

    override fun onActivityStopped(activity: Activity) {

    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        // Used to load the 'myapplication' library on application startup.
        init {
            System.loadLibrary("myapplication")
        }
        var isAppInForeground = false
    }
}