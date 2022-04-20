import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import com.lalilu.util.FilePicker
import com.lalilu.util.rememberFilePicker
import javafx.application.Platform
import javafx.stage.FileChooser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

@Composable
@Preview
fun App() {
    val sourceFile = remember { mutableStateOf<File?>(null) }
    val filePicker: FilePicker = rememberFilePicker()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (sourceFile.value == null) {
            Button(onClick = {
                filePicker.pickFile { sourceFile.value = it }
            }) {
                Text("选择图片")
            }
        } else {
            PicShower(picFile = sourceFile)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowScope.Header(
    onExitApplication: () -> Unit,
    onFullScreen: () -> Unit
) {
    WindowDraggableArea(
        modifier = Modifier.combinedClickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            enabled = true,
            onDoubleClick = onFullScreen,
            onClick = {})
    ) {
        Surface(
            elevation = 15.dp,
            color = Color.Gray
        ) {
            Row(
                modifier = Modifier.height(64.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("图片裁切压缩器", color = Color.White)
                IconButton(onClick = onExitApplication) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource("close-line.svg"),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        visible = true,
        onCloseRequest = ::exitApplication,
        undecorated = true,
        title = "图片裁切压缩器"
    ) {
        MaterialTheme {
            Column {
                Header(onExitApplication = ::exitApplication) {
                    window.placement = when (window.placement) {
                        WindowPlacement.Fullscreen -> WindowPlacement.Floating
                        else -> WindowPlacement.Fullscreen
                    }
                }
                App()
            }
        }
    }
}
