package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.TerminalHistoryItem
import com.example.ui.components.ActionPill
import com.example.ui.components.CyberHeader
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSky
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextCode
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.viewmodel.CyberViewModel
import kotlinx.coroutines.delay

enum class CmdCategory(val label: String) {
    ALL("ALL"),
    CORE("CORE"),
    NETWORK("NET"),
    DEFENSE("DEF"),
    TRIAGE("TRIAGE")
}

data class QuickCmd(val cmd: String, val category: CmdCategory)

@Composable
fun TerminalScreen(viewModel: CyberViewModel) {
    val context = LocalContext.current
    val history by viewModel.terminalHistory.collectAsStateWithLifecycle()
    var cmdInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CmdCategory.ALL) }
    val listState = rememberLazyListState()

    val quickCommands = listOf(
        QuickCmd("help", CmdCategory.CORE),
        QuickCmd("status", CmdCategory.CORE),
        QuickCmd("dns lookup cloudflare.com", CmdCategory.NETWORK),
        QuickCmd("inspect network 1.1.1.1 443", CmdCategory.NETWORK),
        QuickCmd("harden system linux", CmdCategory.DEFENSE),
        QuickCmd("ufw status verbose", CmdCategory.DEFENSE),
        QuickCmd("incident list", CmdCategory.TRIAGE),
        QuickCmd("hash sha256 avatar-core-payload", CmdCategory.CORE)
    )

    val filteredQuickCommands = remember(selectedCategory) {
        if (selectedCategory == CmdCategory.ALL) quickCommands
        else quickCommands.filter { it.category == selectedCategory }
    }

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("terminal_screen")
    ) {
        CyberHeader(
            title = "CYBER TERMINAL",
            subtitle = "Direct cybersecurity command orchestration shell",
            badgeText = "TTY-1 READY",
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val logs = history.joinToString("\n\n") { "darkavatar:~$ ${it.command}\n${it.output}" }
                            clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Logs", logs))
                            Toast.makeText(context, "All session logs copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("export_logs_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export Logs", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("clear") },
                        modifier = Modifier.size(32.dp).testTag("clear_terminal_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Terminal", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        )

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CmdCategory.values().forEach { cat ->
                ActionPill(
                    text = cat.label,
                    selected = selectedCategory == cat
                ) {
                    selectedCategory = cat
                }
            }
        }

        // Quick Command Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filteredQuickCommands.forEach { q ->
                ActionPill(text = q.cmd) {
                    cmdInput = q.cmd
                }
            }
        }

        // Terminal Output Console
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            color = Color(0xFF000000),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(CyberGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TERMINAL FEED",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DARK AVATAR SHELL v4.2 [TTY-1 ONLINE]\nType 'help' for command manual or tap quick commands above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                items(history) { item ->
                    TerminalEntryItem(entry = item)
                }
            }
        }

        // Terminal Prompt Input
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 4.dp),
            color = CyberSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "avatar:~$ ",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = cmdInput,
                    onValueChange = { cmdInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_cmd_input"),
                    placeholder = { Text("type command or select preset...", color = CyberTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = CyberCyan,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (cmdInput.isNotBlank()) {
                            val cmd = cmdInput
                            cmdInput = ""
                            viewModel.executeTerminalCommand(cmd)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberCyan, CircleShape)
                        .testTag("submit_terminal_cmd_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Run", tint = Color(0xFF000000), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TerminalEntryItem(entry: TerminalHistoryItem) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0E14), RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, if (copied) CyberGreen else CyberBorderMuted), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = "darkavatar:~$ ",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = entry.command,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Command Output", entry.output))
                        copied = true
                        Toast.makeText(context, "Output copied", Toast.LENGTH_SHORT).show()
                    },
                color = if (copied) CyberGreen.copy(alpha = 0.2f) else CyberSurfaceVariant,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy output",
                    tint = if (copied) CyberGreen else CyberTextMuted,
                    modifier = Modifier.padding(5.dp).size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.output,
            style = MaterialTheme.typography.bodySmall,
            color = if (entry.isError) CyberCrimson else CyberTextCode,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            fontSize = 11.sp
        )
    }
}

