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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.CodeAuditMode
import com.example.data.model.CodeLanguage
import com.example.data.model.FindingItem
import com.example.ui.components.ActionPill
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CopyableSnippet
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
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
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.viewmodel.CyberViewModel

@Composable
fun CodeAuditScreen(viewModel: CyberViewModel) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.PYTHON) }
    var selectedMode by remember { mutableStateOf(CodeAuditMode.AUDIT) }
    var selectedSeverityFilter by remember { mutableStateOf<AuditSeverity?>(null) }
    var codeText by remember {
        mutableStateOf(
            """import sqlite3
import os

API_KEY = "sk-live-93820194820193820"

def get_user_record(user_input):
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    # Dynamic SQL string concatenation
    query = "SELECT * FROM accounts WHERE id = '" + user_input + "'"
    cursor.execute(query)
    return cursor.fetchall()

def execute_diagnostics(target_host):
    # Unsafe shell command execution
    os.system("ping -c 4 " + target_host)"""
        )
    }

    val activeFindings by viewModel.activeAuditFindings.collectAsStateWithLifecycle()
    val isAuditing by viewModel.isAuditing.collectAsStateWithLifecycle()

    val filteredFindings = remember(activeFindings, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) activeFindings
        else activeFindings.filter { it.severity == selectedSeverityFilter }
    }

    val lineCount = remember(codeText) {
        codeText.count { it == '\n' } + 1
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("code_audit_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            CyberHeader(
                title = "CODE AUDITOR & GENERATOR",
                subtitle = "Multi-language static security analysis, CWE mapping, and secure code hardening",
                badgeText = "STATIC ENGINE"
            )
        }

        // Live Radar when scanning
        item {
            ScannerRadarEffect(
                isScanning = isAuditing,
                label = "AUDITING AST & CWE VULNERABILITY MATRIX"
            )
        }

        // Language Selector
        item {
            Text(
                text = "TARGET LANGUAGE",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
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
                CodeLanguage.values().forEach { lang ->
                    ActionPill(
                        text = lang.displayName,
                        selected = selectedLanguage == lang
                    ) {
                        selectedLanguage = lang
                        if (codeText.isBlank()) {
                            codeText = lang.sampleSnippet
                        }
                    }
                }
            }
        }

        // Mode Selector
        item {
            Text(
                text = "AUDIT OPERATION MODE",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
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
                CodeAuditMode.values().forEach { mode ->
                    ActionPill(
                        text = mode.name,
                        selected = selectedMode == mode
                    ) {
                        selectedMode = mode
                    }
                }
            }
        }

        // Code Editor Input Field
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurfaceVariant,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BUFFER: ${selectedLanguage.displayName.uppercase()} ($lineCount LINES)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (codeText.isNotBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { codeText = "" },
                                    color = Color.Transparent
                                ) {
                                    Text(
                                        text = "CLEAR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCrimson,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = codeText,
                            onValueChange = { codeText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 4.dp)
                                .testTag("code_editor_input"),
                            placeholder = { Text("Paste code snippet to audit...", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp,
                                fontSize = 11.5.sp
                            ),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets row
                Text(
                    text = "LOAD SAMPLE FLAW PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionPill(text = "SQL Injection (Python)") {
                        selectedLanguage = CodeLanguage.PYTHON
                        codeText = """import sqlite3
API_KEY = "sk-live-93820194820193820"
def get_user_record(user_input):
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    query = "SELECT * FROM accounts WHERE id = '" + user_input + "'"
    cursor.execute(query)
    return cursor.fetchall()"""
                    }

                    ActionPill(text = "DOM XSS (JS)") {
                        selectedLanguage = CodeLanguage.JAVASCRIPT
                        codeText = """function renderUserProfile(userData) {
    // Unsanitized innerHTML rendering
    const container = document.getElementById("profile");
    container.innerHTML = "<h3>Welcome " + userData.name + "</h3>";
    eval("var userConfig = " + userData.configPayload);
}"""
                    }

                    ActionPill(text = "Command Exec (Bash)") {
                        selectedLanguage = CodeLanguage.BASH
                        codeText = """#!/bin/bash
TARGET_HOST=${'$'}1
# Unquoted command concatenation
eval "ping -c 3 ${'$'}TARGET_HOST"
/usr/bin/curl -k "https://${'$'}TARGET_HOST/admin""""
                    }

                    ActionPill(text = "Buffer Overflow (C)") {
                        selectedLanguage = CodeLanguage.C
                        codeText = """#include <stdio.h>
#include <string.h>

void process_buffer(char *user_input) {
    char local_buf[64];
    // Insecure bounded memory copy
    strcpy(local_buf, user_input);
}"""
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.runCodeAudit(codeText, selectedLanguage, selectedMode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("run_audit_button"),
                    enabled = !isAuditing && codeText.isNotBlank()
                ) {
                    if (isAuditing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF000000),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXECUTE STATIC SECURITY AUDIT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Findings Output
        if (activeFindings.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IDENTIFIED SECURITY FINDINGS (${filteredFindings.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp
                    )
                }
            }

            item {
                SeverityFilterRow(
                    selectedSeverity = selectedSeverityFilter,
                    onSelectSeverity = { selectedSeverityFilter = it }
                )
            }

            items(filteredFindings) { finding ->
                ExpandableAuditFindingCard(
                    finding = finding,
                    language = selectedLanguage.displayName
                )
            }
        }
    }
}

@Composable
fun ExpandableAuditFindingCard(
    finding: FindingItem,
    language: String
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(true) }

    CyberCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(),
        borderColor = when (finding.severity) {
            AuditSeverity.CRITICAL -> CyberCrimson.copy(alpha = 0.6f)
            AuditSeverity.HIGH -> CyberAmber.copy(alpha = 0.6f)
            AuditSeverity.MEDIUM -> CyberAmber.copy(alpha = 0.5f)
            else -> CyberGreen.copy(alpha = 0.5f)
        },
        cornerRadius = 20
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CWE: ${finding.category} • ${finding.location}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityBadge(severity = finding.severity)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = CyberTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "PROBLEM:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = finding.problem,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextPrimary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SECURITY IMPACT:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCrimson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = finding.whyItMatters,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SECURE REMEDIATION:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = finding.secureFix,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextPrimary,
                        fontSize = 12.sp
                    )

                    if (finding.improvedCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlockView(
                            code = finding.improvedCode,
                            language = language
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = """Vulnerability: ${finding.title}
CWE: ${finding.category}
Location: ${finding.location}
Problem: ${finding.problem}
Fix: ${finding.secureFix}
Code:
${finding.improvedCode}"""
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Vulnerability Report", text))
                                    Toast.makeText(context, "Copied vulnerability details", Toast.LENGTH_SHORT).show()
                                },
                            color = CyberCyan.copy(alpha = 0.12f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "COPY REPORT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

