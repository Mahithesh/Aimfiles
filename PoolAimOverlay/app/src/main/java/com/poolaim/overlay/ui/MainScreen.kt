package com.poolaim.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Panel = Color(0xFF141E29)
private val Muted = Color(0xFF9AA9B8)

@Composable
fun MainScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartFloating: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Practice") }
    val layouts = listOf("Single Cushion Rebound", "Double Cushion Rebound", "Straight Line Practice", "Custom Trick Shot")

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF0B1118),
        bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = Color.White) {
                listOf("Practice", "Layouts", "Settings").forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(if (tab == "Practice") Icons.Default.Layers else if (tab == "Layouts") Icons.Default.Brightness6 else Icons.Default.Tune, null) },
                        label = { Text(tab) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF43D6A4), indicatorColor = Color(0xFF203D38))
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("POOL AIM", color = Color(0xFF43D6A4), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Overlay", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("A clean visual workspace for practicing angles, alignments and rebounds.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, null, tint = if (hasOverlayPermission) Color(0xFF43D6A4) else Color(0xFFFFC16B))
                            Text("Overlay permission", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Surface(color = if (hasOverlayPermission) Color(0xFF173D32) else Color(0xFF49351F), shape = RoundedCornerShape(50)) {
                                Text(if (hasOverlayPermission) "Granted" else "Required", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = if (hasOverlayPermission) Color(0xFF75E5B9) else Color(0xFFFFD08A), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(if (hasOverlayPermission) "Permission is ready. You can start the floating practice tools." else "Allow Pool Aim Overlay to appear above other apps. You can revoke this permission anytime in Android settings.", color = Muted, style = MaterialTheme.typography.bodySmall)
                        if (!hasOverlayPermission) OutlinedButton(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) { Text("Grant overlay permission") }
                        Button(onClick = onStartFloating, enabled = hasOverlayPermission, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Floating Mode", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Text(if (selectedTab == "Layouts") "Practice layouts" else if (selectedTab == "Settings") "Quick settings" else "Start with a layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (selectedTab == "Settings") {
                item { InfoCard("Drawing preferences", "Marker size, guide-line thickness, rectangle border and transparency will be configurable in the floating controls.") }
                item { InfoCard("Privacy", "All practice points and layouts are designed to remain on this device. No game data or game memory is accessed.") }
            } else {
                items(layouts) { name -> LayoutRow(name) }
            }
            item {
                InfoCard("How it works", "Open your practice app, show the floating rectangle, then tap inside it to place reference points. Enable Pan Mode to move the whole practice area. This tool does not automate gameplay.")
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LayoutRow(name: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Layers, null, tint = Color(0xFF43D6A4))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Starter layout", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = Muted, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(body, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
