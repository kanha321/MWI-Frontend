package com.mwi.frontend

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.theme.BasicKolorTheme
import com.mwi.frontend.screens.home.HomeComponentTest
import com.mwi.frontend.screens.home.HomeScreen
import com.mwi.frontend.screens.player.PlayerScreen
import com.mwi.frontend.screens.test.TestScreen
import com.mwi.frontend.screens.upload.UploadScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        PrefsManager.init(this)
        setContent {
            BasicKolorTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Navigator(TestScreen) { navigator ->
                        SlideTransition(navigator)
                    }
                }
            }
        }
    }
}