package com.kanhaji.basics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kanhaji.basics.datastore.PrefsManager
import com.mwi.frontend.AndroidContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidContext.appContext = this
        PrefsManager.init()

        setContent {
//            App()
        }
    }
}
