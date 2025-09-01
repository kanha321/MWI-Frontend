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
import com.mwi.frontend.screens.splash.SplashScreen
import com.mwi.frontend.util.MwiUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        PrefsManager.init(this)
        MwiUtils.deviceId = MwiUtils.getDeviceId(this)
        MwiUtils.appVersionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
        setContent {
            BasicKolorTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Navigator(SplashScreen) { navigator ->
                        SlideTransition(navigator)
                    }
                }
            }
        }
    }
}