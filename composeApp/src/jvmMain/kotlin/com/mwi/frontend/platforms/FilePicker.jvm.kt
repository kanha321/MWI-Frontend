package com.mwi.frontend.platforms

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

actual suspend fun openFilePicker(): String? = withContext(Dispatchers.IO) {
    val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
    fileChooser.dialogTitle = "Select a video file"
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

    // Add video file filter
    val videoFilter = FileNameExtensionFilter(
        "Video files (*.mp4, *.avi, *.mov, *.mkv, *.wmv, *.flv, *.webm)",
        "mp4", "avi", "mov", "mkv", "wmv", "flv", "webm", "m4v", "3gp", "ogv"
    )
    fileChooser.fileFilter = videoFilter
    fileChooser.isAcceptAllFileFilterUsed = false

    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.absolutePath
    } else {
        null
    }
}