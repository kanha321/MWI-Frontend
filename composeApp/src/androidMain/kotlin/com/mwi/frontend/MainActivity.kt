package com.mwi.frontend

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.theme.SystemAppearance
import com.kanhaji.basics.theme.YourAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidContext.appContext = this
        PrefsManager.init()
        setContent {
            SystemAppearance(!isSystemInDarkTheme())
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
object AndroidContext {
    lateinit var appContext: Context
}