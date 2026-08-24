package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuditSeverity
import com.example.data.model.CodeAuditMode
import com.example.data.model.CodeLanguage
import com.example.data.model.FindingItem
import com.example.ui.components.ActionPill
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CopyableSnippet
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.InteractiveSearchBar
import com.example.ui.components.MetricBox
import com.example.ui.components.ScannerRadarEffect
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SeverityFilterRow
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSky
import com.example.ui.theme.CyberSurfaceBright
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.viewmodel.CyberViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: CyberViewModel,
    onNavigateTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val findings by viewModel.allFindings.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf<AuditSeverity?>(null) }
    var isRunningQuickAudit by remember { mutableStateOf(false) }
    var selectedFinding by remember { mutableStateOf<FindingItem?>(null) }

    val filteredFindings = remember(findings, searchQuery, selectedSeverity) {
        findings.filter { f ->
            val matchesSeverity = selectedSeverity == null || f.severity == selectedSeverity
            val matchesQuery = searchQuery.isBlank() ||
                    f.title.contains(searchQuery, ignoreCase = true) ||
                    f.category.contains(searchQuery, ignoreCase = true) ||
                    f.location.contains(searchQuery, ignoreCase = true) ||
                    f.problem.contains(searchQuery, ignoreCase = true)
            matchesSeverity && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            CyberHeader(
                title = "DARK AVATAR",
                subtitle = "DIRECT. FAST. CODE. ANALYZE. DEFEND.",
                badgeText = "SYSTEM ARMED",
                actions = {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    isRunningQuickAudit = true
                                    viewModel.runCodeAudit(
                                        """# Global automated system baseline
API_KEY = "live_sk_prod_9921820"
query = "SELECT * FROM users WHERE id = '" + request.args['id'] + "'"
os.system("ping " + host)""",
                                        CodeLanguage.PYTHON,
                                        CodeAuditMode.AUDIT
                                    )
                                    delay(1200)
                                    isRunningQuickAudit = false
                                    Toast.makeText(context, "System Diagnostic Scan Complete", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("quick_audit_trigger"),
                        color = CyberCyan.copy(alpha = 0.15f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DIAGNOSTIC",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        // Live Radar Effect when scan is running
        item {
            ScannerRadarEffect(
                isScanning = isRunningQuickAudit,
                label = "RUNNING COMPREHENSIVE SECURITY AUDIT"
            )
        }

        // Security Posture Score HUD Card
        item {
            CyberCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                borderColor = if (metrics.criticalFindings > 0) CyberCrimson.copy(alpha = 0.5f) else CyberBorder,
                cornerRadius = 24
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (metrics.securityScore >= 85) CyberGreen else CyberCrimson, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SECURITY POSTURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberTextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                            )
                        }

                        Surface(
                            color = (if (metrics.securityScore >= 85) CyberGreen else CyberCrimson).copy(alpha = 0.12f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, (if (metrics.securityScore >= 85) CyberGreen else CyberCrimson).copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = when {
                                    metrics.securityScore >= 85 -> "FORTIFIED"
                                    metrics.securityScore >= 60 -> "ELEVATED RISK"
                                    else -> "CRITICAL RISK"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (metrics.securityScore >= 85) CyberGreen else CyberCrimson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${metrics.securityScore}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-1).sp
                                ),
                                color = if (metrics.securityScore >= 85) CyberGreen else if (metrics.securityScore >= 60) CyberAmber else CyberCrimson,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " / 100",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberTextMuted,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACTIVE DEFENSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "HEURISTICS SYNCED",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceBright)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(metrics.securityScore / 100f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            CyberCyan,
                                            if (metrics.securityScore >= 85) CyberGreen else CyberCrimson
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Metrics Grid - Fully Interactive Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    title = "Critical",
                    value = "${metrics.criticalFindings}",
                    accentColor = CyberCrimson,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedSeverity = if (selectedSeverity == AuditSeverity.CRITICAL) null else AuditSeverity.CRITICAL
                    }
                )
                MetricBox(
                    title = "High Risk",
                    value = "${metrics.highFindings}",
                    accentColor = CyberAmber,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedSeverity = if (selectedSeverity == AuditSeverity.HIGH) null else AuditSeverity.HIGH
                    }
                )
                MetricBox(
                    title = "Findings",
                    value = "${metrics.totalFindings}",
                    accentColor = CyberSky,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedSeverity = null
                        searchQuery = ""
                    }
                )
                MetricBox(
                    title = "Incidents",
                    value = "${metrics.activeIncidents}",
                    accentColor = CyberGreen,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onNavigateTab(6) // Incidents
                    }
                )
            }
        }

        // Quick Launch Workstation Matrix
        item {
            Text(
                text = "CYBER WORKSTATION SUITE",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SuiteLaunchCard(
                        title = "AI Copilot",
                        subtitle = "Direct Cyber AI",
                        icon = Icons.Default.Psychology,
                        tint = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(1) } // AI
                    )
                    SuiteLaunchCard(
                        title = "Code Engine",
                        subtitle = "Audit & Generate",
                        icon = Icons.Default.Code,
                        tint = CyberGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(3) } // Code
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SuiteLaunchCard(
                        title = "Analyze",
                        subtitle = "Logs, APK, Hash",
                        icon = Icons.Default.Security,
                        tint = CyberSky,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(4) } // Analyze
                    )
                    SuiteLaunchCard(
                        title = "Terminal",
                        subtitle = "Live Cyber Shell",
                        icon = Icons.Default.Terminal,
                        tint = CyberGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(8) } // Terminal
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SuiteLaunchCard(
                        title = "Defense",
                        subtitle = "Hardening & UFW",
                        icon = Icons.Default.Shield,
                        tint = CyberAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(2) } // Defense
                    )
                    SuiteLaunchCard(
                        title = "Network",
                        subtitle = "Ports, DNS, TLS",
                        icon = Icons.Default.Lan,
                        tint = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(5) } // Network
                    )
                }
            }
        }

        // Active Findings / Events
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE FINDINGS REPOSITORY (${filteredFindings.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    fontSize = 10.sp
                )
                if (findings.isNotEmpty()) {
                    Surface(
                        color = CyberCrimson.copy(alpha = 0.12f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CyberCrimson.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { viewModel.clearAllFindings() }
                    ) {
                        Text(
                            text = "CLEAR ALL",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCrimson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Interactive Search and Severity Filters
        item {
            InteractiveSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholderText = "Filter findings by title, category, or problem..."
            )
        }

        item {
            SeverityFilterRow(
                selectedSeverity = selectedSeverity,
                onSelectSeverity = { selectedSeverity = it }
            )
        }

        if (filteredFindings.isEmpty()) {
            item {
                CyberCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (findings.isEmpty()) "NO UNRESOLVED VULNERABILITIES" else "NO MATCHING FINDINGS",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (findings.isEmpty())
                                "Run a Code Audit, Log Analysis, or APK scan to detect and triage security vulnerabilities."
                            else
                                "Try adjusting your search query or severity filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredFindings) { finding ->
                FindingRowItem(
                    finding = finding,
                    onClick = { selectedFinding = finding },
                    onDelete = { viewModel.deleteFinding(finding.id) }
                )
            }
        }
    }

    // Finding Details Dialog
    if (selectedFinding != null) {
        val f = selectedFinding!!
        AlertDialog(
            onDismissRequest = { selectedFinding = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = f.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SeverityBadge(severity = f.severity)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CATEGORY: ${f.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    CopyableSnippet(text = f.location, label = "LOCATION")
                    
                    Text(
                        text = "VULNERABILITY DESCRIPTION:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = f.problem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = "SECURITY IMPACT:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCrimson,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = f.whyItMatters,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                    Text(
                        text = "SECURE REMEDIATION:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = f.secureFix,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextPrimary
                    )
                    if (f.improvedCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CodeBlockView(code = f.improvedCode, language = "FIXED CODE")
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val reportText = """[FINDING REPORT]
Title: ${f.title}
Severity: ${f.severity}
Category: ${f.category}
Location: ${f.location}
Problem: ${f.problem}
Impact: ${f.whyItMatters}
Fix: ${f.secureFix}
Code:
${f.improvedCode}"""
                        clipboard.setPrimaryClip(ClipData.newPlainText("Finding Report", reportText))
                        Toast.makeText(context, "Full Report Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("COPY REPORT", color = CyberGreen, fontFamily = FontFamily.Monospace)
                    }
                    TextButton(onClick = { selectedFinding = null }) {
                        Text("CLOSE", color = CyberCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SuiteLaunchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("launch_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, tint.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun FindingRowItem(
    finding: FindingItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityBadge(severity = finding.severity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = finding.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = finding.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberSky,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = CyberCyan.copy(alpha = 0.12f),
                shape = CircleShape,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "TRIAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

