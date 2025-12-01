package com.jotadev.mediflow

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jotadev.mediflow.ui.theme.MediFlowTheme
import androidx.core.view.WindowCompat
import com.jotadev.mediflow.navigation.AppNavGraph
import com.jotadev.mediflow.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Fuerza iconos claros (blancos) en barra de estado y navegación
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            MediFlowTheme {
                val start = if (intent?.getStringExtra("nav") == "home") "home" else "login"
                AppNavGraph(startDestination = start)

            }
        }

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isGranted = ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }
}
