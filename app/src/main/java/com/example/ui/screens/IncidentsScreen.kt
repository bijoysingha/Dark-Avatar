package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReportProblem
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AuditSeverity
import com.example.data.model.IncidentItem
import com.example.data.model.IncidentStage
import com.example.ui.components.ActionPill
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.InteractiveSearchBar
import com.example.ui.components.SeverityBadge
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
fun IncidentsScreen(viewModel: CyberViewModel) {
    val incidents by viewModel.allIncidents.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStageFilter by remember { mutableStateOf<IncidentStage?>(null) }

    val filteredIncidents = remember(incidents, searchQuery, selectedStageFilter) {
        incidents.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.containmentActions.contains(searchQuery, ignoreCase = true)
            val matchesStage = selectedStageFilter == null || item.stage == selectedStageFilter
            matchesQuery && matchesStage
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("incidents_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CyberHeader(
                    title = "INCIDENT RESPONSE",
                    subtitle = "6-Stage DFIR lifecycle: Detect, Investigate, Contain, Eradicate, Recover, Verify",
                    badgeText = "ACTIVE IR ENGINE"
                )
            }

            // Quick IR Playbook Spawner
            item {
                Text(
                    text = "RAPID RESPONSE PLAYBOOK GENERATOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionPill(text = "+ Ransomware Outbreak") {
                        viewModel.createIncident(
                            title = "Suspected Ransomware File Encryption",
                            description = "Endpoint observed rapid file extension modification, shadow copy deletion, and anomalous disk write IOPS.",
                            severity = AuditSeverity.CRITICAL
                        )
                    }
                    ActionPill(text = "+ Unauthorized SSH Root") {
                        viewModel.createIncident(
                            title = "Unauthorized SSH Root Session Spawned",
                            description = "Brute-force credential stuffing succeeded from an external non-whitelisted ASN.",
                            severity = AuditSeverity.HIGH
                        )
                    }
                    ActionPill(text = "+ Web Shell Backdoor") {
                        viewModel.createIncident(
                            title = "Obfuscated Web Shell in /var/www/uploads",
                            description = "PHP eval backdoor discovered executing base64 payloads via serialized POST parameters.",
                            severity = AuditSeverity.CRITICAL
                        )
                    }
                    ActionPill(text = "+ API Key Exfiltration") {
                        viewModel.createIncident(
                            title = "Production Cloud API Key Leak",
                            description = "High rate of authenticated API queries originating from unrecognized regional endpoints.",
                            severity = AuditSeverity.HIGH
                        )
                    }
                }
            }

            // Search Bar
            item {
                InteractiveSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholderText = "Filter active incidents by keyword..."
                )
            }

            // Stage Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionPill(
                        text = "ALL STAGES",
                        selected = selectedStageFilter == null
                    ) {
                        selectedStageFilter = null
                    }
                    IncidentStage.values().forEach { stage ->
                        ActionPill(
                            text = stage.name,
                            selected = selectedStageFilter == stage
                        ) {
                            selectedStageFilter = stage
                        }
                    }
                }
            }

            if (filteredIncidents.isEmpty()) {
                item {
                    CyberCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        borderColor = CyberBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "NO MATCHING SECURITY INCIDENTS", style = MaterialTheme.typography.titleMedium, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Spawn a rapid response playbook or adjust filters above to track active mitigation lifecycles.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                        }
                    }
                }
            } else {
                items(filteredIncidents) { incident ->
                    IncidentCardItem(
                        incident = incident,
                        onAdvanceStage = { viewModel.advanceIncidentStage(incident) },
                        onDelete = { viewModel.deleteIncident(incident.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // FAB to create custom incident
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .testTag("add_incident_fab"),
            containerColor = CyberCyan,
            contentColor = Color(0xFF000000),
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Incident")
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var severity by remember { mutableStateOf(AuditSeverity.HIGH) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(text = "INITIATE INCIDENT RESPONSE", color = CyberCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Incident Title", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("incident_title_input"),
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
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Initial Threat Triage / Details", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 4.dp).testTag("incident_desc_input"),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActionPill(text = "CRITICAL", selected = severity == AuditSeverity.CRITICAL) { severity = AuditSeverity.CRITICAL }
                        ActionPill(text = "HIGH", selected = severity == AuditSeverity.HIGH) { severity = AuditSeverity.HIGH }
                        ActionPill(text = "MEDIUM", selected = severity == AuditSeverity.MEDIUM) { severity = AuditSeverity.MEDIUM }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createIncident(title, description, severity)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                    shape = CircleShape
                ) {
                    Text("SPAWN PLAYBOOK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun IncidentCardItem(
    incident: IncidentItem,
    onAdvanceStage: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedDetails by remember { mutableStateOf(false) }

    CyberCard(
        modifier = modifier.animateContentSize(),
        borderColor = if (incident.stage == IncidentStage.VERIFY) CyberGreen.copy(alpha = 0.6f) else CyberCrimson.copy(alpha = 0.6f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityBadge(severity = incident.severity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INCIDENT #${incident.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = """INCIDENT #${incident.id}: ${incident.title}
Severity: ${incident.severity}
Stage: ${incident.stage}
Description: ${incident.description}
Containment:
${incident.containmentActions}
Recovery Plan:
${incident.recoveryPlan}"""
                            clipboard.setPrimaryClip(ClipData.newPlainText("Incident Playbook", text))
                            Toast.makeText(context, "Playbook report copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Playbook", tint = CyberCyan, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp).testTag("delete_incident_${incident.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium,
                color = CyberTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodySmall,
                color = CyberTextSecondary,
                fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6-Stage Progress Indicator
            IncidentStageBar(currentStage = incident.stage)

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle Expand Playbook Steps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedDetails = !expandedDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedDetails) "HIDE DFIR ACTIONS" else "VIEW FULL CONTAINMENT & RECOVERY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expandedDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expandedDetails) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = "CONTAINMENT ACTIONS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = incident.containmentActions,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "RECOVERY & REMEDIATION PLAN:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = incident.recoveryPlan,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (incident.stage != IncidentStage.VERIFY) {
                    Button(
                        onClick = onAdvanceStage,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                        shape = CircleShape,
                        modifier = Modifier.testTag("advance_stage_button")
                    ) {
                        Text("ADVANCE STAGE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                } else {
                    Surface(
                        color = CyberGreen.copy(alpha = 0.15f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CyberGreen)
                    ) {
                        Text(
                            text = "RESOLVED & VERIFIED",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncidentStageBar(currentStage: IncidentStage) {
    val stages = IncidentStage.values()
    val currentIndex = stages.indexOf(currentStage)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        stages.forEachIndexed { index, stage ->
            val isActiveOrPassed = index <= currentIndex
            val isCurrent = index == currentIndex

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isCurrent -> CyberCyan
                            isActiveOrPassed -> CyberGreen
                            else -> CyberSurfaceVariant
                        }
                    )
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "STAGE: ${currentStage.name}",
            style = MaterialTheme.typography.labelSmall,
            color = CyberCyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp
        )
    }
}
