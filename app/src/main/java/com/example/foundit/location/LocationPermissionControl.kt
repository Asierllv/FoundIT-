package com.example.foundit.location

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

/** Compact location control shown inside the Explore search field. */
@Composable
fun LocationPermissionControl(
    onLocation: (Location) -> Unit,
    onManualFallback: () -> Unit,
    onUnavailable: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            val location = DeviceLocation.lastKnown(context)
            if (location != null) onLocation(location) else {
                onUnavailable()
                onManualFallback()
            }
        } else {
            onUnavailable()
            onManualFallback()
        }
    }

    IconButton(onClick = {
        if (DeviceLocation.hasPermission(context)) {
            val location = DeviceLocation.lastKnown(context)
            if (location != null) onLocation(location) else {
                onUnavailable()
                onManualFallback()
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }) {
        Text("⌖", color = Color(0xFF234B3A), fontSize = 25.sp)
    }
}
