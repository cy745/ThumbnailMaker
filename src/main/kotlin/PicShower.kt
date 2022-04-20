import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asDesktopBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.util.FilePicker
import com.lalilu.util.component.DragAndScrollableCanvas
import com.lalilu.util.component.FlowTextField
import com.lalilu.util.rememberFilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.coobird.thumbnailator.Thumbnailator
import net.coobird.thumbnailator.Thumbnails
import org.jetbrains.skia.Bitmap
import java.io.File
import kotlin.math.roundToInt

fun File.toBitmap(): ImageBitmap? {
    if (!exists()) return null
    return try {
        loadImageBitmap(this.inputStream())
    } catch (e: Exception) {
        null
    }
}

fun File.toBitmapPainter(): BitmapPainter? {
    if (!exists()) return null
    return this.toBitmap()?.let { BitmapPainter(it) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PicShower(
    scope: CoroutineScope = rememberCoroutineScope(),
    picFile: MutableState<File?>,
    filePicker: FilePicker = rememberFilePicker()
) {
    val bitmap = remember(picFile.value) { picFile.value?.toBitmap() }
    val menuPanelShow = remember { mutableStateOf(true) }
    val bitmapDraw = remember { mutableStateOf(false) }
    val handleBitmapEnable = remember { mutableStateOf(true) }

    val targetWidth = remember { mutableStateOf("0") }
    val targetHeight = remember { mutableStateOf("0") }
    val targetX = remember { mutableStateOf(0f) }
    val targetY = remember { mutableStateOf(0f) }
    val targetScaleValue = remember { mutableStateOf(0f) }

    val rectSize = remember(targetWidth.value, targetHeight.value) {
        val size = try {
            Size(targetWidth.value.toFloat(), targetHeight.value.toFloat())
        } catch (e: Exception) {
            Size(240f, 240f)
        }
        mutableStateOf(size)
    }


    Row {
        Box(
            modifier = Modifier.fillMaxHeight()
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .background(color = Color(0xFFAEAEAE))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (bitmap != null) {
                    DragAndScrollableCanvas(
                        bitmap = bitmap,
                        rectSize = rectSize.value,
                        rectX = targetX,
                        rectY = targetY,
                        scaleValue = targetScaleValue,
                        bitmapDraw = bitmapDraw
                    )
                } else {
                    Text(text = "加载失败: ${picFile.value?.absoluteFile}")
                }
            }
            IconToggleButton(
                modifier = Modifier.align(Alignment.TopEnd),
                checked = menuPanelShow.value,
                onCheckedChange = menuPanelShow::value::set
            ) {
                Text(
                    text = if (menuPanelShow.value) ">" else "<",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
            IconButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = {
                    bitmapDraw.value = false
                }
            ) {
                Text(
                    text = "||",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }
        AnimatedVisibility(visible = menuPanelShow.value) {
            Surface(elevation = 5.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 300.dp, max = 500.dp)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("${picFile.value?.path}")
                    Button(onClick = {
                        filePicker.pickFile { it?.let { picFile.value = it } }
                    }) {
                        Text("重新选择图片")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PreferenceCardForText("目标宽度", targetWidth)
                        PreferenceCardForText("目标高度", targetHeight)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowTextField("X起点", targetX)
                        FlowTextField("Y起点", targetY)
                    }
                    FlowTextField("缩放比例", targetScaleValue)
                    Button(
                        enabled = handleBitmapEnable.value,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                handleBitmapEnable.value = false
                                try {
                                    println("开始压缩")
                                    Thumbnails.of(picFile.value)
                                        .scale(targetScaleValue.value.toDouble())
                                        .sourceRegion(
                                            (targetX.value / targetScaleValue.value).roundToInt(),
                                            (targetY.value / targetScaleValue.value).roundToInt(),
                                            (rectSize.value.width / targetScaleValue.value).roundToInt(),
                                            (rectSize.value.height / targetScaleValue.value).roundToInt()
                                        ).toFile("D:\\Desktop\\output.png")
                                } catch (e: Exception) {
                                    println(e.message)
                                } finally {
                                    handleBitmapEnable.value = true
                                }
                            }
                        }) {
                        Text("生成图片")
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceCardForText(title: String, value: MutableState<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Surface(
            color = Color.LightGray,
            shape = RoundedCornerShape(5.dp)
        ) {
            BasicTextField(
                modifier = Modifier.padding(5.dp),
                value = value.value,
                singleLine = true,
                maxLines = 1,
                onValueChange = {
                    if (it.length >= 6) return@BasicTextField
                    value.value = it
                })
        }
    }
}