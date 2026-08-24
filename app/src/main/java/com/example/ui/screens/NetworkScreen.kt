package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActionPill
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
fun NetworkScreen(viewModel: CyberViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("PORT PROBE", "HTTP HEADERS", "DNS LOOKUP", "CIDR CALC")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("network_screen")
    ) {
        CyberHeader(
            title = "NETWORK DIAGNOSTICS",
            subtitle = "Authorized port probes, TLS header inspection, DNS resolution, and subnet planning",
            badgeText = "SOCKET PROBE"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberDarkSurface,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == index) CyberCyan else CyberTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> PortProbeTab(viewModel)
            1 -> HttpHeadersTab(viewModel)
            2 -> DnsLookupTab(viewModel)
            3 -> CidrCalcTab()
        }
    }
}

@Composable
fun PortProbeTab(viewModel: CyberViewModel) {
    var host by remember { mutableStateOf("127.0.0.1") }
    var portsString by remember { mutableStateOf("80, 443, 22, 8080, 3306") }
    var isAuthorizedChecked by remember { mutableStateOf(false) }

    val results by viewModel.portProbeResults.collectAsStateWithLifecycle()
    val isScanning by viewModel.isNetworkScanning.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CyberCard(borderColor = CyberAmber) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authorized Penetration Testing & Lab Assessment Only. Only probe hosts/IPs you own or have explicit authorization to test.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Target Host / IP", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("probe_host_input"),
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

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = portsString,
                    onValueChange = { portsString = it },
                    label = { Text("Ports to Probe (comma-separated)", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("probe_ports_input"),
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "Web Suite (80,443,8080)") {
                    portsString = "80, 443, 8080, 8443"
                }
                ActionPill(text = "Remote Access (22,3389,5900)") {
                    portsString = "22, 3389, 5900, 23"
                }
                ActionPill(text = "Databases (3306,5432,27017)") {
                    portsString = "3306, 5432, 27017, 6379, 1433"
                }
                ActionPill(text = "Active Directory (53,88,389,445)") {
                    portsString = "53, 88, 135, 139, 389, 445, 636"
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAuthorizedChecked,
                    onCheckedChange = { isAuthorizedChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = CyberGreen, checkmarkColor = Color(0xFF000000)),
                    modifier = Modifier.testTag("authorized_checkbox")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I confirm I have explicit authorization to test this endpoint.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextPrimary
                )
            }
        }

        item {
            Button(
                onClick = {
                    val portList = portsString.split(",").mapNotNull { it.trim().toIntOrNull() }
                    viewModel.probeAuthorizedPorts(host, portList)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                shape = CircleShape,
                enabled = isAuthorizedChecked && !isScanning && host.isNotBlank(),
                modifier = Modifier.fillMaxWidth().testTag("start_port_probe_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF000000), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("RUN AUTHORIZED PORT PROBE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        item {
            com.example.ui.components.ScannerRadarEffect(
                isScanning = isScanning,
                label = "PROBING ACTIVE SOCKETS ON $host"
            )
        }

        if (results.isNotEmpty()) {
            item {
                Text(
                    text = "PROBE RESULTS (${results.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(results) { (port, desc) ->
                val isOpen = desc.contains("OPEN")
                CyberCard(borderColor = if (isOpen) CyberGreen else CyberBorderMuted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PORT $port",
                                style = MaterialTheme.typography.titleSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )
                        }

                        Surface(
                            color = if (isOpen) CyberGreen.copy(alpha = 0.15f) else CyberDarkSurface,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isOpen) CyberGreen else CyberBorderMuted)
                        ) {
                            Text(
                                text = if (isOpen) "OPEN" else "CLOSED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOpen) CyberGreen else CyberTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HttpHeadersTab(viewModel: CyberViewModel) {
    var targetUrl by remember { mutableStateOf("https://example.com") }
    val results by viewModel.httpHeaderResults.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "INSPECT HTTP & TLS SECURITY HEADERS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace
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
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Target URL / Host", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("http_url_input"),
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

        item {
            Button(
                onClick = { viewModel.inspectHttpHeaders(targetUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().testTag("run_http_inspect_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("INSPECT HEADERS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (results != null) {
            val res = results!!
            if (res.containsKey("error")) {
                item {
                    CyberCard(borderColor = CyberCrimson) {
                        Text(text = "${res["error"]}", color = CyberCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                val present = res["presentHeaders"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val missing = res["missingHeaders"] as? List<*> ?: emptyList<Any>()

                item {
                    CyberCard(borderColor = CyberCyan) {
                        Column {
                            Text(text = "HTTP STATUS: ${res["status"]}", color = CyberGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "SERVER DISCLOSURE: ${res["server"]}", color = CyberSky, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Text(text = "CONFIGURED SECURITY HEADERS (${present.size})", style = MaterialTheme.typography.labelSmall, color = CyberGreen, fontFamily = FontFamily.Monospace)
                }

                items(present.entries.toList()) { entry ->
                    CyberCard(borderColor = CyberGreen) {
                        Column {
                            Text(text = "${entry.key}", color = CyberCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "${entry.value}", color = CyberTextPrimary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                if (missing.isNotEmpty()) {
                    item {
                        Text(text = "MISSING SECURITY HEADERS (${missing.size})", style = MaterialTheme.typography.labelSmall, color = CyberCrimson, fontFamily = FontFamily.Monospace)
                    }

                    items(missing) { h ->
                        CyberCard(borderColor = CyberCrimson) {
                            Text(text = "[-] $h (Not configured — recommended for hardening)", color = CyberCrimson, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DnsLookupTab(viewModel: CyberViewModel) {
    var domain by remember { mutableStateOf("cloudflare.com") }
    val dnsResults by viewModel.dnsResults.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "QUERY DNS RECORDS & IP RESOLUTION", style = MaterialTheme.typography.labelSmall, color = CyberTextMuted, fontFamily = FontFamily.Monospace)
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain Name", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("dns_domain_input"),
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

        item {
            Button(
                onClick = { viewModel.resolveDns(domain) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF000000)),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().testTag("run_dns_lookup_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESOLVE DNS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        if (dnsResults.isNotEmpty()) {
            item {
                Text(text = "RESOLVED IP ADDRESSES", style = MaterialTheme.typography.labelSmall, color = CyberTextMuted, fontFamily = FontFamily.Monospace)
            }

            items(dnsResults) { record ->
                CyberCard(borderColor = CyberCyan) {
                    Text(text = record, color = CyberTextPrimary, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CidrCalcTab() {
    var ipInput by remember { mutableStateOf("192.168.1.100") }
    var prefixLength by remember { mutableStateOf("24") }

    val prefix = prefixLength.toIntOrNull() ?: 24
    val clampedPrefix = prefix.coerceIn(1, 32)

    val totalHosts = if (clampedPrefix >= 31) 2 else (1L shl (32 - clampedPrefix))
    val usableHosts = if (clampedPrefix >= 31) 0 else (totalHosts - 2).coerceAtLeast(0)

    val netmask = when (clampedPrefix) {
        8 -> "255.0.0.0"
        16 -> "255.255.0.0"
        24 -> "255.255.255.0"
        25 -> "255.255.255.128"
        26 -> "255.255.255.192"
        27 -> "255.255.255.224"
        28 -> "255.255.255.240"
        29 -> "255.255.255.248"
        30 -> "255.255.255.252"
        32 -> "255.255.255.255"
        else -> "/$clampedPrefix"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "SUBNET & CIDR ARCHITECTURE CALCULATOR", style = MaterialTheme.typography.labelSmall, color = CyberTextMuted, fontFamily = FontFamily.Monospace)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurfaceVariant,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("IP Address", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurfaceVariant,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    OutlinedTextField(
                        value = prefixLength,
                        onValueChange = { prefixLength = it },
                        label = { Text("CIDR (1-32)", color = CyberCyan, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "/24 DMZ (254 hosts)") {
                    ipInput = "192.168.1.0"
                    prefixLength = "24"
                }
                ActionPill(text = "/28 Bastion (14 hosts)") {
                    ipInput = "10.0.50.0"
                    prefixLength = "28"
                }
                ActionPill(text = "/16 Cloud VPC (65k hosts)") {
                    ipInput = "172.16.0.0"
                    prefixLength = "16"
                }
                ActionPill(text = "/30 Link (2 hosts)") {
                    ipInput = "10.255.255.0"
                    prefixLength = "30"
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    title = "Total IPs",
                    value = "$totalHosts",
                    accentColor = CyberSky,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Usable Hosts",
                    value = "$usableHosts",
                    accentColor = CyberGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CyberCard(borderColor = CyberCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "NETWORK SPECIFICATION", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "CIDR NOTATION: $ipInput/$clampedPrefix", style = MaterialTheme.typography.bodyMedium, color = CyberTextPrimary, fontFamily = FontFamily.Monospace)
                    Text(text = "SUBNET MASK: $netmask", style = MaterialTheme.typography.bodyMedium, color = CyberGreen, fontFamily = FontFamily.Monospace)
                    Text(text = "HOST RANGE: ${usableHosts} usable endpoints for secure segmentation.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                }
            }
        }
    }
}
