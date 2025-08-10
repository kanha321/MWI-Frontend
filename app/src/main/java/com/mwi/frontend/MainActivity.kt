package com.mwi.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.kanhaji.basics.theme.BasicKolorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PrefsManager.init(this)
        setContent {
            BasicKolorTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Navigator(SettingsScreen) { navigator ->
                        SlideTransition(navigator)
                    }
                }
            }
        }
    }
}