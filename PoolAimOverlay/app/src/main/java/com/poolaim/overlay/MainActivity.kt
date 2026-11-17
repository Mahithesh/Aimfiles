package com.poolaim.overlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import com.poolaim.overlay.overlay.FloatingOverlayService
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.poolaim.overlay.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF43D6A4),
                    secondary = Color(0xFF79A9FF),
                    background = Color(0xFF0B1118),
                    surface = Color(0xFF141E29),
                    onPrimary = Color(0xFF06130F),
                    onBackground = Color(0xFFEAF0F6),
                    onSurface = Color(0xFFEAF0F6)
                )
            ) {
                MainScreen(
                    hasOverlayPermission = Settings.canDrawOverlays(this),
                    onRequestPermission = {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")))
                    },
                    onStartFloating = {
                        if (Settings.canDrawOverlays(this)) {
                            val service = Intent(this, FloatingOverlayService::class.java)
                            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service) else startService(service)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
