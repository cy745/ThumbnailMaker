package com.lalilu.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javafx.application.Platform
import javafx.stage.FileChooser
import java.io.File

@Composable
fun rememberFilePicker(): FilePicker {
    return remember { FilePicker.getInstance() }
}

class FilePicker private constructor() {
    private val chooser = FileChooser()

    init {
        try {
            val file = File(System.getProperty("user.home") + "/Pictures")
                .takeIf { it.isDirectory } ?: File(System.getProperty("user.home"))
            chooser.initialDirectory = file
        } catch (e: Exception) {
        }
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Only Images", "*.jpg", "*.png", "*.jpeg", "*.bmp"),
            FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
            FileChooser.ExtensionFilter("PNG", "*.png"),
            FileChooser.ExtensionFilter("BMP", "*.bmp")
        )
    }

    fun pickFile(callback: (File?) -> Unit) {
        try {
            Platform.startup { callback(chooser.showOpenDialog(null)) }
        } catch (e: Exception) {
            Platform.runLater { callback(chooser.showOpenDialog(null)) }
        }
    }

    companion object {
        private var instance: FilePicker? = null

        fun getInstance(): FilePicker {
            instance = instance ?: synchronized(FilePicker::class.java) {
                instance ?: FilePicker()
            }
            return instance!!
        }
    }
}


