package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSky
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.viewmodel.CyberViewModel

@Composable
fun SettingsScreen(viewModel: CyberViewModel) {
    var showPurgeDialog by remember { mutableStateOf(false) }

    val hasApiKey = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (e: Exception) {
        false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CyberHeader(
                title = "SETTINGS & TELEMETRY",
                subtitle = "Workstation specifications, privacy controls, and AI orchestration engine",
                badgeText = "SYSTEM v2.5"
            )
        }

        // AI Engine Status
        item {
            CyberCard(borderColor = CyberCyan) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "AI CYBER COPILOT ENGINE", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            color = if (hasApiKey) CyberGreen.copy(alpha = 0.15f) else CyberAmber.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (hasApiKey) CyberGreen else CyberAmber)
                        ) {
                            Text(
                                text = if (hasApiKey) "ONLINE (GEMINI)" else "OFFLINE HEURISTICS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasApiKey) CyberGreen else CyberAmber,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasApiKey) {
                            "Dark Avatar is powered by Gemini 3.5 Flash with custom cybersecurity system directives. Fast response streaming and code analysis active."
                        } else {
                            "Running built-in high-performance local offline heuristic engine for static code audits, log parsing, hash identification, and system hardening rules."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                }
            }
        }

        // Security & Privacy Policy
        item {
            CyberCard(borderColor = CyberGreen) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "LOCAL DATA & ZERO TELEMETRY", style = MaterialTheme.typography.titleMedium, color = CyberGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• All audit findings, reports, and incident response playbooks are stored in the local Room SQLite database on your device.\n• No unauthorized telemetry, tracking, or user data is dispatched to external advertising or analytics brokers.\n• Network socket probes require explicit user authorization confirmation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                }
            }
        }

        // Directives & Authorized Penetration Testing
        item {
            CyberCard(borderColor = CyberAmber) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "AUTHORIZED PENETRATION TESTING", style = MaterialTheme.typography.titleMedium, color = CyberAmber, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dark Avatar is designed for defensive cybersecurity, authorized penetration testing, security research, system hardening, and secure software development. Never perform security assessments against systems or networks without explicit legal authorization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                }
            }
        }

        // Database Purge / Reset
        item {
            CyberCard(borderColor = CyberCrimson) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "PURGE LOCAL WORKSTATION DATA", style = MaterialTheme.typography.titleSmall, color = CyberCrimson, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Permanently wipe all findings, incidents, reports, and terminal history.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showPurgeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson, contentColor = Color.White),
                            shape = CircleShape,
                            modifier = Modifier.testTag("purge_all_data_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PURGE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = {
                Text("CONFIRM DATABASE PURGE", color = CyberCrimson, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            },
            text = {
                Text(
                    "Are you sure you want to purge all local security findings, incident response timelines, compiled reports, and terminal history? This operation is irreversible.",
                    color = CyberTextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purgeAllData()
                        showPurgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson, contentColor = Color.White),
                    shape = CircleShape
                ) {
                    Text("YES, PURGE EVERYTHING", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("CANCEL", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
