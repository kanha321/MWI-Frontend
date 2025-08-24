package com.mwi.frontend.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoAdultContent
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

object AppSettingsItems {
    var showNsfwContent by mutableStateOf(false)

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

    fun get(): List<SettingItems> {

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
        return items
    }
}