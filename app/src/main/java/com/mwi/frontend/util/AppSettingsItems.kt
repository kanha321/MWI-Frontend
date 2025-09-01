package com.mwi.frontend.util

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.NoAdultContent
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kanhaji.basics.composables.KSwitch
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.entity.SettingItems
import com.mwi.frontend.datastore.AppPrefsResources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.util.Updater
import com.mwi.frontend.entity.Update
import io.ktor.client.call.body
import io.ktor.client.request.get

object AppSettingsItems {
    var showNsfwContent by mutableStateOf(false)
    var preferUnwatchedVideos by mutableStateOf(true)
    var isUpdateAvailable by mutableStateOf(false)
    var update by mutableStateOf<Update?>(null)

    val scope = CoroutineScope(Dispatchers.IO)

    private fun switchNsfwContent(enabled: Boolean) {
        showNsfwContent = enabled
        scope.launch {
            PrefsManager.saveBoolean(
                AppPrefsResources.NSFW,
                showNsfwContent
            )
        }
    }

    private fun switchPreferUnwatched(enabled: Boolean) {
        preferUnwatchedVideos = enabled
        scope.launch {
            PrefsManager.saveBoolean(
                AppPrefsResources.PREFER_UNWATCHED_VIDEOS,
                preferUnwatchedVideos
            )
        }
    }

    private fun openGitHubRepo(context: Context) {
        // Implementation to open GitHub repository link
        val url = "https://kanha321.github.io/pages/mwi"

        // open the url in a browser
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url.toUri()
        context.startActivity(intent)
    }

    fun get(context: Context): List<SettingItems> {

        val items = mutableListOf<SettingItems>()
        items.add(
            SettingItems(
                id = "nsfw_content",
                title = "Mutthi Mode",
                description = "#Mutthi Without Internet",
                icon = Icons.Outlined.NoAdultContent,
                widget = {
                    KSwitch(
                        state = showNsfwContent,
                        onCheckedChange = { switchNsfwContent(it) }
                    )
                },
                onClick = { switchNsfwContent(!showNsfwContent) }
            )
        )
        items.add(
            SettingItems(
                id = "prefer_unwatched",
                title = "Unwatched First",
                description = "Show unwatched videos first in the feed",
                icon = if (preferUnwatchedVideos) Icons.Outlined.VideocamOff else Icons.Outlined.Videocam,
                widget = {
                    KSwitch(
                        state = preferUnwatchedVideos,
                        onCheckedChange = { switchPreferUnwatched(it) }
                    )
                },
                onClick = { switchPreferUnwatched(!preferUnwatchedVideos) }
            )
        )
//        items.add(
//            SettingItems(
//                id
//            )
//        )
//        items.add(
//            SettingItems(
//                id = "github_repo",
//                title = "GitHub Repository",
//                description = "View the source code on GitHub",
//                icon = Icons.Outlined.Code,
//                onClick = {
//                    openGitHubRepo(context)
//                }
//            )
//        )
        return items
    }
}