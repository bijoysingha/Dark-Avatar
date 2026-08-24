package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReportItem
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.MetricBox
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
fun ReportsScreen(viewModel: CyberViewModel) {
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val findings by viewModel.allFindings.collectAsStateWithLifecycle()
    var showGenerateDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<ReportItem?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("reports_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CyberHeader(
                    title = "SECURITY REPORTS",
                    subtitle = "Automated executive briefings, vulnerability matrices, and Markdown/JSON export",
                    badgeText = "REPORT ENGINE"
                )
            }

            // Summary Card
            item {
                CyberCard(borderColor = CyberCyan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE FINDINGS IN SCOPE: ${findings.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Compile all stored static code, manifest, and log findings into a unified executive security report.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showGenerateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                            shape = CircleShape,
                            modifier = Modifier.testTag("generate_report_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COMPILE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (reports.isEmpty()) {
                item {
                    CyberCard(borderColor = CyberBorderMuted) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CyberSky, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "NO REPORTS COMPILED YET", style = MaterialTheme.typography.titleMedium, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Tap 'COMPILE' to generate your first technical audit report from current active findings.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                        }
                    }
                }
            } else {
                items(reports) { report ->
                    CyberCard(borderColor = CyberBorder) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, report.contentMarkdown)
                                                putExtra(Intent.EXTRA_SUBJECT, report.title)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Share Cyber Audit Report")
                                            context.startActivity(shareIntent)
                                        },
                                        modifier = Modifier.size(24.dp).testTag("share_report_${report.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { viewModel.deleteReport(report.id) },
                                        modifier = Modifier.size(24.dp).testTag("delete_report_${report.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SCOPE: ${report.scope}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.executiveSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MetricBox(
                                    title = "Total",
                                    value = "${report.findingsCount}",
                                    accentColor = CyberSky,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    title = "Critical",
                                    value = "${report.criticalCount}",
                                    accentColor = CyberCrimson,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    title = "High",
                                    value = "${report.highCount}",
                                    accentColor = CyberAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { selectedReport = report },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant, contentColor = CyberCyan),
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth().testTag("view_report_${report.id}")
                            ) {
                                Text("INSPECT FULL REPORT (MARKDOWN / JSON)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGenerateDialog) {
        var reportTitle by remember { mutableStateOf("Full Security Architecture Assessment") }
        var reportScope by remember { mutableStateOf("Production Infrastructure & Application Source") }
        var customSummary by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            title = {
                Text("COMPILE AUDIT REPORT", color = CyberCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CyberSurfaceVariant,
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        OutlinedTextField(
                            value = reportTitle,
                            onValueChange = { reportTitle = it },
                            label = { Text("Report Title", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("report_title_input"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberTextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = CyberCyan,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CyberSurfaceVariant,
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        OutlinedTextField(
                            value = reportScope,
                            onValueChange = { reportScope = it },
                            label = { Text("Audit Scope", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("report_scope_input"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberTextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = CyberCyan,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CyberSurfaceVariant,
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        OutlinedTextField(
                            value = customSummary,
                            onValueChange = { customSummary = it },
                            label = { Text("Custom Executive Summary (Optional)", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 4.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberTextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = CyberCyan,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportTitle.isNotBlank()) {
                            viewModel.generateReport(reportTitle, reportScope, customSummary)
                            showGenerateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                    shape = CircleShape
                ) {
                    Text("GENERATE REPORT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }) {
                    Text("CANCEL", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (selectedReport != null) {
        val rep = selectedReport!!
        var viewTab by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { selectedReport = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = rep.title, color = CyberCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabRow(
                        selectedTabIndex = viewTab,
                        containerColor = CyberSurfaceVariant,
                        contentColor = CyberCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[viewTab]),
                                color = CyberCyan,
                                height = 2.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = viewTab == 0,
                            onClick = { viewTab = 0 },
                            text = { Text("MARKDOWN", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = if (viewTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (viewTab == 0) CyberCyan else CyberTextMuted) }
                        )
                        Tab(
                            selected = viewTab == 1,
                            onClick = { viewTab = 1 },
                            text = { Text("JSON STRUCTURE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = if (viewTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (viewTab == 1) CyberCyan else CyberTextMuted) }
                        )
                    }

                    if (viewTab == 0) {
                        CodeBlockView(code = rep.contentMarkdown, language = "MARKDOWN", modifier = Modifier.height(300.dp))
                    } else {
                        CodeBlockView(code = rep.contentJson, language = "JSON", modifier = Modifier.height(300.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedReport = null }) {
                    Text("CLOSE", color = CyberCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
