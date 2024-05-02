package com.example.myapplication

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class ImageViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {
    var selectedImageUri = MutableLiveData<Uri?>()
    lateinit var srcBitmap: Bitmap
    lateinit var bmBuffer: ByteBuffer

    init {
        val uriString = sharedPreferences.getString(KEY_SELECTED_IMAGE_URI, null)
        val uri = uriString?.let { Uri.parse(it) }
        selectedImageUri.value = uri
    }


    fun setCurrentImageUri(uri: Uri?) {
        selectedImageUri.value = uri
        sharedPreferences.edit().putString(KEY_SELECTED_IMAGE_URI, uri?.toString()).apply()
    }

    companion object {
        private const val KEY_SELECTED_IMAGE_URI = "selected_image_uri"
    }
}


/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */


/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */


fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

private external fun imgBlur(
    inBuf: ByteBuffer,
    outBuf:ByteBuffer,
    width: Int,
    height: Int,
    sigma: Float
)
private external fun onedGaussBlur(
    inBuf: ByteBuffer,
    outBuf:ByteBuffer,
    width: Int,
    height: Int,
    kernelSize:Int,
    sigma: Float
)

private external fun bufferInCPP(
    byteBuffer: ByteBuffer?,
    newBuffer:ByteBuffer?,
    startX: Int,
    startY: Int,
    width: Int,
    height: Int,
    ogWidth :Int,
    ogHeight :Int
)

private external fun memCopyInCpp(
    inBuf: ByteBuffer?,
    outBuf:ByteBuffer?,
    capacity: Long
)

private external fun stackBlur(
    byteBuffer: ByteBuffer?,
    newBuffer:ByteBuffer?,
    width: Int,
    height: Int,
    rad: Int
)

external fun scaleWithAvir(inBuf:ByteBuffer,
                           outBuf: ByteBuffer,
                           width:Int,height: Int,
                           scaleFactor:Float, bitResolution:Int)

private external fun bilinearScale(inBuf:ByteBuffer,
                                   outBuf: ByteBuffer,
                                   width:Int,height: Int,
                                   newWidth:Int, newHeight:Int)


private external fun imgTransparent(inBuf:ByteBuffer, capacity:Long,alpha:Float)



fun writeSelectedImagePart(imageViewModel: ImageViewModel,
                           bitmap: Bitmap?,
                           viewSize: IntSize,
                           imageSize: IntSize,
                           scale:Float, zoom:Float,
                           originalOffset: Offset,
                           newOffset: Offset,
                           selectorPercent:Float,
                           originalBuffer: ByteBuffer
){
    if (bitmap == null) {
        return
    }
    val screenImgRatio =
        (viewSize.height * selectorPercent / imageSize.height) * scale / zoom

    Log.d("initial zoom", zoom.toString())
    val originalWidth = imageSize.width
    val originalHeight = imageSize.height
    val imageActualWidth =
        (viewSize.width * selectorPercent / screenImgRatio).roundToInt()
    val imageActualHeight =
        (viewSize.height * selectorPercent / screenImgRatio).roundToInt()

    val newBitmap =
        Bitmap.createBitmap(imageActualWidth, imageActualHeight, bitmap.config)
    val newBuffer =
        ByteBuffer.allocateDirect(newBitmap.byteCount)
    val startX = (abs(newOffset.x + viewSize.width*(1-selectorPercent)/2)/ screenImgRatio).roundToInt()
    val startY = ((abs(newOffset.y) ) / screenImgRatio).roundToInt()

    //Log.d("offsety"," ${offset.value.y} $startY ${inset} $imageWidthOnScreen")
    bufferInCPP(
        originalBuffer,
        newBuffer,
        startX,
        startY,
        imageActualWidth,
        imageActualHeight,
        originalWidth,//original width of image
        originalHeight//original height of image
    )
    imageViewModel.bmBuffer = newBuffer
    newBitmap.copyPixelsFromBuffer(newBuffer)
    imageViewModel.srcBitmap = newBitmap
}

fun rotateOffset(
    x: Float,
    y:Float,
    angle: Float): Offset
{
    val coerceAngle =  (angle + 180) % 360 - 180
    val angleInRadians = coerceAngle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

fun reverseRotateOffset(
    offset: Offset,
    angle: Float
): Offset
{
    //val coerceAngle =  (angle + 180) % 360 - 180
    //val angleInDegrees = (angle + 360) % 360
    val angleInRadians = ((angle+180) %360 -180) * PI / 180
    return Offset(
        (offset.x * cos(angleInRadians) + offset.y * sin(angleInRadians)).toFloat(),
        (-offset.x * sin(angleInRadians) + offset.y * cos(angleInRadians)).toFloat()
    )
}

fun minZoomToFit(
    width: Float,
    height: Float,
    angle: Float,
    imgWidth: Float,
    imgHeight: Float): Offset {
    val diagonal = sqrt((width * width + height * height))
    // Calculate the angle between the diagonal and the width (or height)
    val alphaDegree = Math.toDegrees(atan2(width, height).toDouble())

    // Calculate the angle of rotation for the selector
    var minSelectorRotateAngleForWidth =  90 - angle - alphaDegree

    var minSelectorRotateAngleForHeight = alphaDegree -angle

    // Normalize the angle to be within the range of -180 to 180 degrees
    minSelectorRotateAngleForHeight = (minSelectorRotateAngleForHeight + 180) % 360 - 180
    minSelectorRotateAngleForWidth = (minSelectorRotateAngleForWidth + 180) % 360 - 180

    // Calculate the cosine of the rotation angle
    val cosBetaHeight = cos(minSelectorRotateAngleForHeight* PI / 180)
    val cosBetaWidth = cos( minSelectorRotateAngleForWidth * PI /180)

    val minRequiredWidth = cosBetaWidth*diagonal
    val minRequiredHeight = cosBetaHeight*diagonal

    val requiredWidthZoomToFit = minRequiredWidth/imgWidth
    val requiredHeightZoomToFit = minRequiredHeight/imgHeight

    // Calculate the minimum required width after rotation
    return Offset(requiredWidthZoomToFit.toFloat(),requiredHeightZoomToFit.toFloat())
}


//coercing the angle to 90 for geometrical calculation
fun coerceAngle(angle:Float) : Float{
    val absoluteAngle = angle.absoluteValue
    return if(absoluteAngle >90){
        180- absoluteAngle
    }else{
        absoluteAngle
    }
}

//I have no idea what the hell am I trying to solve anymore
fun rectangleBound(angle:Float,
                   sw:Float,//selector width
                   sh: Float,//selector height
                   width: Float,
                   height: Float) : Array<Offset> {
    val coercedAngle = (angle + 180) % 360 - 180 //(angle % 180 + 180) % 180 - 90
    val angleInRadians = coercedAngle * PI / 180

    val imgCenter = Offset((width / 2), (height / 2))

    val cosAlpha = cos(angleInRadians).toFloat()
    val sinAlpha = sin(angleInRadians).toFloat()

   //current point ranges (a,b), (c,d), (e,f), (g,h)
    val a = Offset(-width/2,               height/2 - sinAlpha*sw)
    val b = Offset(-width/2,              -height/2 + cosAlpha*sh)
    val c = Offset(-width/2 + sinAlpha*sh,-height/2              )
    val d = Offset( width/2 - cosAlpha*sw,-height/2              )
    val e = Offset( width/2,              -height/2 + sinAlpha*sw)
    val f = Offset( width/2,               height/2 - cosAlpha*sh)
    val g = Offset(-width/2 + cosAlpha*sw, height/2              )
    val h = Offset( width/2 - sinAlpha*sh, height/2              )

    //center post rotate b
    val centerPostRotate = reverseRotateOffset(imgCenter,angle)

    //post rotate range and translate from center space to original space
    val aPostRot = reverseRotateOffset(a, angle) - centerPostRotate
    val bPostRot = reverseRotateOffset(b, angle) - centerPostRotate
    val cPostRot = reverseRotateOffset(c, angle) - centerPostRotate
    val dPostRot = reverseRotateOffset(d, angle) - centerPostRotate
    val ePostRot = reverseRotateOffset(e, angle) - centerPostRotate
    val fPostRot = reverseRotateOffset(f, angle) - centerPostRotate
    val gPostRot = reverseRotateOffset(g, angle) - centerPostRotate
    val hPostRot = reverseRotateOffset(h, angle) - centerPostRotate

    val yRange = arrayOf(aPostRot,bPostRot,cPostRot,dPostRot,ePostRot,fPostRot,gPostRot,hPostRot).sortedWith(compareBy({ it.y }, { it.x }))
    val xRange = arrayOf(aPostRot,bPostRot,cPostRot,dPostRot,ePostRot,fPostRot,gPostRot,hPostRot).sortedWith(compareBy({ it.x }, { it.y }))

    Log.d("range bound", "$yRange ")

    val horLeftBound = xRange[0]
    val horRightBound = xRange[7]

    val verTopBound  = yRange[0]
    val verBottomBound = yRange[7]

    return arrayOf(
        centerPostRotate,
        horLeftBound,
        horRightBound,
        verTopBound,
        verBottomBound,
    )
}


@Composable
fun ThemeCustomization(currentView: MutableState<ViewType>,
                       imageViewModel: ImageViewModel,
                       uriImg: MutableState<Uri?>,
) {
    BackHandler {
        currentView.value = ViewType.NAVIGATION
    }
    val context = LocalContext.current
    var maxSize by remember { mutableStateOf(IntSize(0,0)) }

    val selectorPercent = 0.7f

    val imageLoader = ImageLoader(context)
    var imageSize by remember { mutableStateOf(IntSize(0, 0)) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var originalOffset by remember { mutableStateOf(Offset.Zero) }
    val angle = remember { mutableFloatStateOf(0f) }

    var originalRatio by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    //var rotation = remember { mutableStateOf(0f) }
    val finishGettingImgSize = remember { mutableStateOf(false) }
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    val drawableState = remember { mutableStateOf<Drawable?>(null) }
    val originalBuffer = remember { mutableStateOf(ByteBuffer.allocate(0)) }

    var imgRotate by remember { mutableStateOf(false) }
    val infiniteSliderPos = remember{ mutableFloatStateOf(0f) }

    val scrollState = rememberScrollableState { delta ->
        if (imgRotate) {
            angle.floatValue -= delta/30
        }
        0f
    }

    // Load the image and retrieve its size
    LaunchedEffect(Unit) {
        System.loadLibrary("myapplication")
        val request = ImageRequest.Builder(context)
        .data(uriImg.value)
        .allowHardware(false)
        .build()
        // Start a coroutine to fetch the image size
        val drawable = withContext(Dispatchers.IO) {
            imageLoader.execute(request).drawable
        }
        drawableState.value = drawable
        val width = drawable?.intrinsicWidth ?: 0
        val height = drawable?.intrinsicHeight ?:0
        imageSize = IntSize(width, height)

        originalRatio = height * 1f / width * 1f
        val bmp=
            drawable?.toBitmap()
        if (bmp!=null) {
            bitmapState.value = bmp
            originalBuffer.value = ByteBuffer.allocateDirect(bmp.byteCount)
            bmp.copyPixelsToBuffer(originalBuffer.value)
        }
        finishGettingImgSize.value = true
    }

    var translatedImgWidth by remember{ mutableFloatStateOf(0f) }
    var translatedImgHeight by remember{ mutableFloatStateOf(0f) }
    var selector by remember { mutableStateOf(Rect(Offset.Zero, Size.Zero)) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        // note: scale goes by factor, not an absolute difference, so we need to multiply it
        Log.d("offset" ,"${offset+Offset(translatedImgWidth,translatedImgHeight)}")
        offset -= offsetChange
        scale = if (zoom >=1){
            (scale * zoomChange).coerceIn(0.7f*zoom..4f*zoom)
        }else{
            (scale * zoomChange).coerceIn(0.7f..4f)
        }
        if (imgRotate){
            angle.floatValue = (angle.floatValue + rotationChange)
        }
    }

    LaunchedEffect(state.isTransformInProgress, scrollState.isScrollInProgress) {
        if (state.isTransformInProgress || scrollState.isScrollInProgress || zoom == 1f) {
            return@LaunchedEffect
        }
        if (false) {
            //image display size post scale
            val imgWidthOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.width.toFloat()
                } else {
                    maxSize.width.toFloat()
                }
            val imgHeightOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.height.toFloat()
                } else {
                    maxSize.width * originalRatio
                }
            val coerceAngle = coerceAngle(angle.floatValue.absoluteValue)
            val minZoomHAndW =
                minZoomToFit(
                    maxSize.width * selectorPercent,
                    maxSize.height * selectorPercent,
                    coerceAngle(coerceAngle),
                    imgWidthOnScreen * scale,
                    imgHeightOnScreen * scale,
                )

            //minZoomHAnW.x is minimum width for the image to fit in the selector, minZoomHAndW.y is the minimum height for the image to fit
            val requiredZoom =
                if (minZoomHAndW.x<minZoomHAndW.y){
                    minZoomHAndW.y
                }else{
                    minZoomHAndW.x
                }
                //max(minZoomHAndW.y,minZoomHAndW.x)
            if (requiredZoom >1) {
                val currentScale = scale
                scale = currentScale*requiredZoom
                launch(Dispatchers.IO) {
                    animate(
                        initialValue = currentScale,
                        targetValue = currentScale*requiredZoom,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        scale = value
                    }
                }
            }
            translatedImgWidth = if (imageSize.width < maxSize.width) {
                0.5f * (imageSize.width * scale - imageSize.width)
            } else {
                0.5f * (maxSize.width * scale - maxSize.width)
            }

            translatedImgHeight = if (imageSize.width < maxSize.width) {
                0.5f * ( imageSize.height * scale - imageSize.height)
            } else {
                0.5f * (maxSize.width * originalRatio * scale - maxSize.width * originalRatio)
            }

            val verticalOffset = (maxSize.height - maxSize.height * selectorPercent) / 2

            val bound =
                    rectangleBound(
                    coerceAngle,
                        maxSize.width*selectorPercent,
                        maxSize.height*selectorPercent,
                        imgWidthOnScreen * scale,
                        imgHeightOnScreen * scale,
                )
            val imgCenterB4Rotate = bound[0]
            val horLeftBound  = bound[1]//min
            val horRightBound = bound[2]//max
            val verBottBound    = bound[3]//min
            val verUpBound  = bound[4]//max

            val ogOffset = reverseRotateOffset(offset, angle.floatValue)
            Log.d("rotate", "current offset ${offset} b4rotate${ogOffset}")

            val imageWidthOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.width * scale
                } else {
                    maxSize.width * scale
                }
            val imageHeightOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.height * scale
                } else {
                    maxSize.width * originalRatio * scale
                }

            translatedImgWidth = if (imageSize.width < maxSize.width) {
                0.5f * (imageWidthOnScreen - imageSize.width)
            } else {
                0.5f * (imageWidthOnScreen - maxSize.width)
            }

            translatedImgHeight = if (imageSize.width < maxSize.width) {
                0.5f * (imageHeightOnScreen - imageSize.height)
            } else {
                0.5f * (imageHeightOnScreen - maxSize.width * originalRatio)
            }

            val boundY = ogOffset.y + translatedImgHeight + maxSize.height*selectorPercent
            val boundX = 0.85f * maxSize.width

            val targetValueX = if (ogOffset.x + translatedImgWidth < -0.15f * maxSize.width) {
                -0.15f * maxSize.width - translatedImgWidth
            } else if (ogOffset.x + translatedImgWidth > (imageWidthOnScreen - boundX)) {
                imageWidthOnScreen -boundX - translatedImgWidth
            } else {
                ogOffset.x
            }

            val targetValueY = if (ogOffset.y + translatedImgHeight < 0) {
                -translatedImgHeight
            } else if (boundY - imageHeightOnScreen > 0) {
                ogOffset.y - (boundY - imageHeightOnScreen)
            } else {
                ogOffset.y
            }
            val vertical = targetValueY != ogOffset.y
            val horizontal = targetValueX != ogOffset.x

            //Image flow back vertical
            if (vertical) {
                launch {
                    animate(
                        initialValue = offset.y,
                        targetValue = offset.y + targetValueY - ogOffset.y,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        offset = Offset(offset.x, value)
                    }
                }
            }
            //Image flow back horizontal
            if (horizontal) {
                launch {
                    animate(
                        initialValue = offset.x,
                        targetValue = offset.x + targetValueX - ogOffset.x,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        offset = Offset(value, offset.y)
                    }
                }
            }
            //0.4f*maxSize.width/scale+ translatedImgWidth/scale, 0.4f*maxSize.height-verticalOffset
        }
        else {
            if (scale < zoom) {
                val currentScale = scale
                scale = zoom
                launch(Dispatchers.IO) {
                    animate(
                        initialValue = currentScale,
                        targetValue = zoom,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        scale = value
                    }
                }
            }

            val imageWidthOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.width * scale
                } else {
                    maxSize.width * scale
                }
            val imageHeightOnScreen =
                if (imageSize.width < maxSize.width) {
                    imageSize.height * scale
                } else {
                    maxSize.width * originalRatio * scale
                }

            translatedImgWidth = if (imageSize.width < maxSize.width) {
                0.5f * (imageWidthOnScreen - imageSize.width)
            } else {
                0.5f * (imageWidthOnScreen - maxSize.width)
            }

            translatedImgHeight = if (imageSize.width < maxSize.width) {
                0.5f * (imageHeightOnScreen - imageSize.height)
            } else {
                0.5f * (imageHeightOnScreen - maxSize.width * originalRatio)
            }

            val boundY = offset.y + translatedImgHeight + maxSize.height*selectorPercent
            val boundX = 0.85f * maxSize.width

            val targetValueX = if (offset.x + translatedImgWidth < -0.15f * maxSize.width) {
                -0.15f * maxSize.width - translatedImgWidth
            } else if (offset.x + translatedImgWidth > (imageWidthOnScreen - boundX)) {
                 imageWidthOnScreen -boundX - translatedImgWidth
            } else {
                offset.x
            }

            val targetValueY = if (offset.y + translatedImgHeight < 0) {
               -translatedImgHeight
            } else if (boundY - imageHeightOnScreen > 0) {
                offset.y - (boundY - imageHeightOnScreen)
            } else {
                offset.y
            }
            val vertical = targetValueY != offset.y
            val horizontal = targetValueX != offset.x

            //Image flow back vertical
            if (vertical) {
                launch {
                    animate(
                        initialValue = offset.y,
                        targetValue = targetValueY,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        offset = Offset(offset.x, value)
                    }
                }
            }
            //Image flow back horizontal
            if (horizontal) {
                launch {
                    animate(
                        initialValue = offset.x,
                        targetValue = targetValueX,
                        animationSpec = tween(durationMillis = 500)
                    ) { value, _ ->
                        offset = Offset(value, offset.y)
                    }
                }
            }
        }
    }

    if (finishGettingImgSize.value) {
        val size = Size(maxSize.width * 1f, maxSize.height * 1f)
        val heightFromWidth = size.width * originalRatio
        val ogImageHeight = imageSize.height
        val ogImageWidth = imageSize.width
        // Image with width greater than composable width is scaled to composable width size, Image with width
        // less than composable does not
        if (ogImageWidth < size.width) {
            Log.d("imageSizeless", "$ogImageWidth $zoom ${offset.x - 0.5f*ogImageWidth*(zoom-1)} ")
            zoom = size.height * selectorPercent / ogImageHeight
            val initialAdjustAmountX = (size.width / scale  - ogImageWidth )/2
            scale *= zoom
            offset = Offset(offset.x  - initialAdjustAmountX , offset.y - 0.5f*ogImageHeight*(scale-1))
            originalOffset = offset
        } else {
            Log.d("imageSizeLarger", "$ogImageHeight$ogImageWidth")
            zoom = size.height * selectorPercent / heightFromWidth
            val initialAdjustAmountX = (size.width/scale  - maxSize.width )/2
            scale *= zoom
            offset = Offset(offset.x - initialAdjustAmountX , offset.y - 0.5f*heightFromWidth*(scale-1))
            originalOffset = offset
        }
        finishGettingImgSize.value = false
    }
    val bottomBarSize by remember { mutableStateOf(20.dp) }
    val imageSelected = remember{ mutableStateOf(false) }
    ProvideWindowInsets {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(rememberInsetsPaddingValues(insets = LocalWindowInsets.current.systemBars)),)
        {
            val (topBar, bottomBarLeft, bottomBar, bottomBarRight) = createRefs()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp)
                    .constrainAs(topBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        //height = Dimension.fillToConstraints
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        uriImg.value = imageViewModel.selectedImageUri.value
                        currentView.value = ViewType.NAVIGATION
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        imgRotate = !imgRotate
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh
                        ,
                        contentDescription = "Rotate",
                        tint = if (imgRotate) {Color.White} else {MaterialTheme.colorScheme.primary}
                    )
                }

                IconButton(
                    onClick = {
                        imageSelected.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Replace with the "tick" icon
                        contentDescription = "Tick",
                        tint = Color.White
                    )
                    LaunchedEffect(imageSelected.value) {
                        if (imageSelected.value) {
                            writeSelectedImagePart(
                                imageViewModel,
                                bitmapState.value,
                                maxSize, imageSize,
                                scale, zoom,
                                originalOffset,
                                Offset(
                                    offset.x + translatedImgWidth,
                                    offset.y + translatedImgHeight
                                ),
                                selectorPercent,
                                originalBuffer.value,
                            )
                            currentView.value = ViewType.BACKGROUNDPREVIEW
                        }
                    }
                }
            }

            AnimatedVisibility(
                imgRotate,
                Modifier
                    .fillMaxWidth()
                    .height(bottomBarSize * 3)
                    .padding(start = 4.dp, end = 4.dp)
                    .scrollable(
                        orientation = Orientation.Horizontal,
                        state = scrollState
                    )
                    .constrainAs(bottomBar) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Row(
                    Modifier.background(Color.White.copy(0.4f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            angle.floatValue *=-1
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear, // Replace with the "tick" icon
                            contentDescription = "clear rotate",
                            tint = Color.White
                        )
                    }
                    Column(
                        // modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        // verticalArrangement = Arrangement.Center
                    )
                    {
                        Text(
                            text = " ${"%.1f".format(angle.floatValue)}\u00B0", // Including the degree symbol using Unicode escape
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall // Adjust the style as needed
                        )
                        InfiniteScrollingRectangles(angle, bottomBarSize)
                    }
                    IconButton(
                        onClick = {

                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, // Replace with the "tick" icon
                            contentDescription = "T",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { size ->
            maxSize = size
            val topLeft = Offset(0.15f * size.width,0f)
            val rectSize = Size(size.width * selectorPercent, size.height * selectorPercent)
            selector = Rect(topLeft, rectSize)
        }
        .background(color = Color.Black)
        .transformable(state = state)
        .graphicsLayer {
        // initial scaling to fit the selector
        translationX = -offset.x
        translationY = -offset.y
        val verticalOffset = (maxSize.height - maxSize.height * selectorPercent) / 2

        translationY += verticalOffset
        scaleX = scale
        scaleY = scale
        rotationZ = angle.floatValue
        //offset = reverseRotateOffset( scaleTransOffset - imgTransOffset ,angle.floatValue) + imgTransOffset
        TransformOrigin(0.5f, 0.5f).also { transformOrigin = it }
    }

    ) {
        Image(
            painter = rememberAsyncImagePainter(
                drawableState.value,
                imageLoader
            ),
            contentDescription = "Background Image",
            modifier = Modifier
                /*
                .graphicsLayer {
                    // initial scaling to fit the selector
                    translationX = -offset.x
                    translationY = -offset.y
                    val verticalOffset = (maxSize.height - maxSize.height * selectorPercent) / 2

                    translationY += verticalOffset
                    scaleX = scale
                    scaleY = scale


                    rotationZ = angle.floatValue
                    //offset = reverseRotateOffset( scaleTransOffset - imgTransOffset ,angle.floatValue) + imgTransOffset
                    TransformOrigin(0.5f, 0.5f).also { transformOrigin = it }
                }

                 */
                /*
                .drawWithContent {
                    drawContent()

                    val imageWidthOnScreen =
                        if (imageSize.width < maxSize.width) {
                            imageSize.width * scale
                        } else {
                            maxSize.width * scale
                        }
                    val imageHeightOnScreen =
                        if (imageSize.width < maxSize.width) {
                            imageSize.height * scale
                        } else {
                            maxSize.width * originalRatio * scale
                        }

                    translatedImgWidth = if (imageSize.width < maxSize.width) {
                        0.7f * (imageWidthOnScreen - imageSize.width)
                    } else {
                        0.7f * (imageWidthOnScreen - maxSize.width)
                    }
                    val rectSize = Size(maxSize.width*selectorPercent/scale, maxSize.height*selectorPercent/scale)
                    val verticalOffset =
                        (maxSize.height - maxSize.height * selectorPercent) / 2
                    val innerRectangle = Rectangle(0.4f*maxSize.width/scale+ translatedImgWidth/scale, 0.4f*maxSize.height-verticalOffset, maxSize.width*selectorPercent, maxSize.height*selectorPercent, 0f) // Not rotated
                    val topLeft =
                        Offset(
                            (maxSize.width - rectSize.width) / 2 ,
                            (maxSize.height - rectSize.height) / 2 //- bottomBarSize.toPx()
                        )
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(innerRectangle.x,innerRectangle.y),
                        size = rectSize,
                        style = Stroke(width = 10f)
                    )
                }

                 */
        )
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = angle.floatValue
                TransformOrigin(0.5f, 0.5f).also { transformOrigin = it }
            }
        ) {
            drawIntoCanvas { canvas ->
                val rectSize = Size(size.width * selectorPercent, size.height * selectorPercent)
                val topLeft =
                    Offset(
                        (size.width - rectSize.width) / 2 ,
                        (size.height - rectSize.height) / 2 //- bottomBarSize.toPx()
                    )
                val canvasPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }
                val smallRectanglePath = Path().apply {
                    addRect(Rect(topLeft, size = rectSize))
                }
                val clippedPath = Path().apply {
                    op(canvasPath, smallRectanglePath, PathOperation.Difference)
                }
                val paint = Paint().apply {
                    color = Color.Black.copy(alpha = 0.5f)
                }
                // Fill the clipped path with the paint object
                canvas.drawPath(clippedPath, paint)
                // Draw the border
                drawRect(
                    color = Color.LightGray,
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Composable
fun InfiniteScrollingRectangles(
    position : MutableFloatState,
    height: Dp
) {
    val lineCount = 20
    if (position.floatValue >= 180) {
        position.floatValue= -180f
    } else if (position.floatValue < -180) {
        position.floatValue = 180f
    }


    Canvas(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(height)
                //.background(Color.White)
    ) {

        // Draw lines continuously to create an infinite loop
        for (index in 0 until lineCount + 1) {
            var startX = index * size.width / lineCount  + (position.floatValue*3)

            if (startX < 0) {
                startX = size.width - startX.absoluteValue
            }else if (startX > size.width) {
                startX -= (startX/ size.width).toInt()*size.width
            }

            drawLine(
                color = Color.White,
                start = Offset(startX, size.height/2),
                end = Offset(startX, size.height*3/4),
                strokeWidth = 1.dp.toPx() // Adjust the width for line thickness
            )
            // Reset the offset when reaching the end to create an infinite loop
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BackgroundPreview(currentView: MutableState<ViewType>, imageViewModel: ImageViewModel, uriImg: MutableState<Uri?> ) {
    val srcBitmapBeforeScale by remember {
        mutableStateOf(imageViewModel.srcBitmap)
    }
    val srcBitmap = remember { mutableStateOf(imageViewModel.srcBitmap) }

    val bufferArray =
        remember { mutableStateOf(Array<ByteBuffer>(8) { ByteBuffer.allocateDirect(0) }) }
    val bufferArrayAlter =
        remember { mutableStateOf(Array<ByteBuffer>(7) { ByteBuffer.allocateDirect(0) }) }
    bufferArray.value[0] = imageViewModel.bmBuffer

    var isScale by remember {
        mutableStateOf(false)
    }
    var srcbmWidth by remember { mutableIntStateOf(srcBitmap.value.width) }
    var srcbmHeight by remember { mutableIntStateOf(srcBitmap.value.height) }

    val context = LocalContext.current
    val alpha = remember { mutableFloatStateOf(0f) }
    val prevBlurRadius = remember { mutableFloatStateOf(0f) }

    val close = remember { mutableStateOf(false) }

    Log.d("BufferSize", imageViewModel.bmBuffer.capacity().toString())
    // Initialize this as an empty buffer with the same size as the originalBuffer
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            bufferArrayAlter.value[0] = bufferArray.value[0]
            for (alter in 1 until bufferArrayAlter.value.size) {
                bufferArrayAlter.value[alter] = ByteBuffer.allocateDirect((srcBitmap.value.byteCount))
                stackBlur(
                    bufferArrayAlter.value[alter - 1],
                    bufferArrayAlter.value[alter],
                    srcbmWidth,
                    srcbmHeight,
                    10
                )
                if (close.value) {
                    return@withContext
                }
            }

            bufferArray.value[1] = bufferArrayAlter.value[bufferArrayAlter.value.size - 1]
            for (i in 2 until bufferArray.value.size) {
                bufferArray.value[i] = ByteBuffer.allocateDirect((srcBitmap.value.byteCount))
                stackBlur(
                    bufferArray.value[i - 1],
                    bufferArray.value[i],
                    srcbmWidth,
                    srcbmHeight,
                    10
                )
                if (close.value) {
                    return@withContext
                }
            }
        }
    }


    /*
        DisposableEffect(Unit) {
            // Release cached image buffers or other resources here
            onDispose {
                bufferArray.value.forEach { it.clear() } // Optional, clear the buffers.
                bufferArray.value = emptyArray()
            }
        }

     */


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor("#342c2c")))
    )
    {
        ProvideWindowInsets() {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawIntoCanvas {
                    // Draw a transparent background with the specified alphaValue
                    val destinationRect =
                        android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())

                    drawImage(
                        srcBitmap.value.asImageBitmap(),
                        dstOffset = IntOffset(destinationRect.left, destinationRect.top),
                        dstSize = IntSize(destinationRect.width(), destinationRect.height())
                    )
                    drawRect(color = Color.Black.copy(alpha = alpha.floatValue))
                }
            }

            Column(
                modifier = Modifier
                    .padding(rememberInsetsPaddingValues(insets = LocalWindowInsets.current.systemBars))
            )
            {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                currentView.value = ViewType.NAVIGATION
                                uriImg.value = imageViewModel.selectedImageUri.value
                                close.value = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        val selected = remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                selected.value = true
                                imgTransparent(
                                    imageViewModel.bmBuffer,
                                    imageViewModel.bmBuffer.capacity().toLong(),
                                    alpha.floatValue
                                )
                                val out = File(context.filesDir, "bg.png")
                                imageViewModel.bmBuffer.rewind()
                                if (alpha.floatValue != 0f) {
                                    srcBitmap.value.copyPixelsFromBuffer(imageViewModel.bmBuffer)
                                }
                                out.writeBitmap(
                                    srcBitmap.value,
                                    Bitmap.CompressFormat.JPEG,
                                    100
                                )
                                imageViewModel.setCurrentImageUri(out.toUri())
                                uriImg.value = out.toUri()
                                currentView.value = ViewType.NAVIGATION
                            }
                        ) {
                            LaunchedEffect(selected.value) {
                                launch {
                                    withContext(Dispatchers.IO) {
                                        val fileToDelete = File(context.filesDir, "newbg.png")
                                        if (fileToDelete.exists()) {
                                            fileToDelete.delete()
                                        } else {
                                            // File does not exist
                                        }
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Check, // Replace with the "tick" icon
                                contentDescription = "Tick",
                                tint = Color.White
                            )
                        }

                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )


                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Text(
                            text = "Alpha",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth(0.15f)
                        )

                        Slider(
                            value = alpha.value,
                            onValueChange = { newAlpha ->
                                alpha.value = newAlpha
                            },
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        Text(
                            text = "Blur",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth(0.15f)
                        )

                        Slider(
                            value = prevBlurRadius.value,
                            onValueChange = { newBlurRad ->
                                val clampedValue =
                                    newBlurRad.coerceIn(0f, bufferArray.value.size - 1f)

                                if (newBlurRad == 0f) {
                                    //imageViewModel.bmBuffer = bufferArray.value[0]
                                    //if (imageViewModel.bmBuffer.capacity()>= srcBitmap.value.byteCount) {
                                        srcBitmap.value =  srcBitmapBeforeScale
                                    //}
                                }
                                if (clampedValue > 0 && clampedValue < 2f) {
                                    val bfsize = bufferArrayAlter.value.size
                                    val newBuffer =
                                        bufferArrayAlter.value[(clampedValue / 2 * bfsize).toInt()]
                                    if ((clampedValue * bfsize).toInt() != (prevBlurRadius.floatValue * bfsize).toInt()) {
                                        imageViewModel.bmBuffer = newBuffer
                                        imageViewModel.bmBuffer.rewind()
                                        if (newBuffer.capacity() >= srcBitmap.value.byteCount) {
                                            srcBitmap.value.copyPixelsFromBuffer(imageViewModel.bmBuffer)
                                        }
                                    }
                                }

                                if (clampedValue >= 2 && clampedValue.toInt() != prevBlurRadius.floatValue.toInt()) {
                                    imageViewModel.bmBuffer = bufferArray.value[clampedValue.toInt()]
                                    imageViewModel.bmBuffer.rewind()
                                    if (imageViewModel.bmBuffer.capacity()>= srcBitmap.value.byteCount){
                                        srcBitmap.value.copyPixelsFromBuffer(imageViewModel.bmBuffer)
                                    }
                                }
                                prevBlurRadius.floatValue = clampedValue
                            },
                            valueRange = 0f..bufferArray.value.size - 1f, // Set the valid range for the Slider
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}






