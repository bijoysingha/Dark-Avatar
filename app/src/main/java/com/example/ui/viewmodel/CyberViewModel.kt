package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.ai.OfflineSecurityEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AuditSeverity
import com.example.data.model.ChatMessage
import com.example.data.model.CodeAuditMode
import com.example.data.model.CodeLanguage
import com.example.data.model.CyberMetrics
import com.example.data.model.FindingItem
import com.example.data.model.IncidentItem
import com.example.data.model.IncidentStage
import com.example.data.model.ReportItem
import com.example.data.model.TerminalHistoryItem
import com.example.data.repository.CyberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CyberViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CyberRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = CyberRepository(db)
    }

    val metrics: StateFlow<CyberMetrics> = repository.metrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CyberMetrics())

    val allFindings: StateFlow<List<FindingItem>> = repository.allFindings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIncidents: StateFlow<List<IncidentItem>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportItem>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val terminalHistory: StateFlow<List<TerminalHistoryItem>> = repository.terminalHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                content = "DARK AVATAR CYBERSECURITY WORKSTATION ACTIVATED.\n\nType a technical prompt, paste logs or code to audit, or select a mode to begin defensive analysis.",
                mode = "SYSTEM_READY"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    fun sendAiMessage(prompt: String, mode: String = "DEFENSIVE_CODER") {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(isUser = true, content = prompt, mode = mode)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiGenerating.value = true
            val response = GeminiClient.queryGemini(prompt)
            val aiMsg = ChatMessage(isUser = false, content = response, mode = mode)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiGenerating.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                isUser = false,
                content = "DARK AVATAR AI CONTEXT CLEARED.\n\nReady for new cybersecurity analysis or code generation task.",
                mode = "SYSTEM_READY"
            )
        )
    }

    // --- Log Analysis State ---
    private val _logAnalysisResult = MutableStateFlow<Map<String, Any>?>(null)
    val logAnalysisResult: StateFlow<Map<String, Any>?> = _logAnalysisResult.asStateFlow()

    fun analyzeLogs(logText: String) {
        if (logText.isBlank()) return
        viewModelScope.launch {
            val result = OfflineSecurityEngine.parseSecurityLogs(logText)
            _logAnalysisResult.value = result
        }
    }

    // --- Hash & Crypto State ---
    private val _hashResults = MutableStateFlow<Map<String, String>>(emptyMap())
    val hashResults: StateFlow<Map<String, String>> = _hashResults.asStateFlow()

    private val _identifiedHashTypes = MutableStateFlow<List<String>>(emptyList())
    val identifiedHashTypes: StateFlow<List<String>> = _identifiedHashTypes.asStateFlow()

    private val _entropyResult = MutableStateFlow<Map<String, Any>>(emptyMap())
    val entropyResult: StateFlow<Map<String, Any>> = _entropyResult.asStateFlow()

    fun computeHashesAndEntropy(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _hashResults.value = OfflineSecurityEngine.calculateHashes(input)
            _identifiedHashTypes.value = OfflineSecurityEngine.identifyHashType(input)
            _entropyResult.value = OfflineSecurityEngine.calculateEntropy(input)
        }
    }

    // --- Code Auditor State ---
    private val _activeAuditFindings = MutableStateFlow<List<FindingItem>>(emptyList())
    val activeAuditFindings: StateFlow<List<FindingItem>> = _activeAuditFindings.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    fun runCodeAudit(code: String, language: CodeLanguage, mode: CodeAuditMode) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _isAuditing.value = true
            // Run real local rule-based static engine
            val localFindings = OfflineSecurityEngine.auditCodeLocally(code, language)
            _activeAuditFindings.value = localFindings

            // Auto-persist high risk findings to Room database
            for (f in localFindings) {
                if (f.severity != AuditSeverity.INFO) {
                    repository.insertFinding(f)
                }
            }
            _isAuditing.value = false
        }
    }

    fun saveCustomFinding(finding: FindingItem) {
        viewModelScope.launch {
            repository.insertFinding(finding)
        }
    }

    fun deleteFinding(id: Long) {
        viewModelScope.launch {
            repository.deleteFinding(id)
        }
    }

    fun clearAllFindings() {
        viewModelScope.launch {
            repository.clearFindings()
            _activeAuditFindings.value = emptyList()
        }
    }

    // --- Android APK Manifest Inspector ---
    private val _manifestFindings = MutableStateFlow<List<FindingItem>>(emptyList())
    val manifestFindings: StateFlow<List<FindingItem>> = _manifestFindings.asStateFlow()

    fun auditAndroidManifest(xml: String) {
        if (xml.isBlank()) return
        viewModelScope.launch {
            val findings = OfflineSecurityEngine.auditAndroidManifest(xml)
            _manifestFindings.value = findings
            for (f in findings) {
                if (f.severity != AuditSeverity.INFO) {
                    repository.insertFinding(f)
                }
            }
        }
    }

    // --- Malware Static Analyzer ---
    private val _malwareAnalysisResult = MutableStateFlow<Map<String, Any>?>(null)
    val malwareAnalysisResult: StateFlow<Map<String, Any>?> = _malwareAnalysisResult.asStateFlow()

    fun analyzeMalwareStrings(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val result = OfflineSecurityEngine.analyzeMalwareStrings(content)
            _malwareAnalysisResult.value = result
        }
    }

    // --- Network Security & Diagnostics ---
    private val _portProbeResults = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val portProbeResults: StateFlow<List<Pair<Int, String>>> = _portProbeResults.asStateFlow()

    private val _dnsResults = MutableStateFlow<List<String>>(emptyList())
    val dnsResults: StateFlow<List<String>> = _dnsResults.asStateFlow()

    private val _httpHeaderResults = MutableStateFlow<Map<String, Any>?>(null)
    val httpHeaderResults: StateFlow<Map<String, Any>?> = _httpHeaderResults.asStateFlow()

    private val _isNetworkScanning = MutableStateFlow(false)
    val isNetworkScanning: StateFlow<Boolean> = _isNetworkScanning.asStateFlow()

    fun probeAuthorizedPorts(host: String, ports: List<Int>) {
        if (host.isBlank() || ports.isEmpty()) return
        viewModelScope.launch {
            _isNetworkScanning.value = true
            val results = mutableListOf<Pair<Int, String>>()
            for (port in ports) {
                val (isOpen, desc) = OfflineSecurityEngine.probePort(host.trim(), port)
                results.add(Pair(port, desc))
            }
            _portProbeResults.value = results
            _isNetworkScanning.value = false
        }
    }

    fun resolveDns(domain: String) {
        if (domain.isBlank()) return
        viewModelScope.launch {
            _dnsResults.value = OfflineSecurityEngine.resolveDns(domain.trim())
        }
    }

    fun inspectHttpHeaders(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _httpHeaderResults.value = OfflineSecurityEngine.inspectHttpHeaders(url.trim())
        }
    }

    // --- Incident Response Engine ---
    fun createIncident(title: String, description: String, severity: AuditSeverity) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newIncident = IncidentItem(
                title = title,
                description = description,
                stage = IncidentStage.DETECT,
                severity = severity,
                affectedSystems = "Target Cluster / Endpoint",
                iocs = "Awaiting forensic extraction",
                containmentActions = "1. Isolate network interface\n2. Dump active process tree\n3. Revoke active session tokens",
                recoveryPlan = "1. Clean restore from verified backup\n2. Apply patched build\n3. Rotate secrets"
            )
            repository.insertIncident(newIncident)
        }
    }

    fun advanceIncidentStage(incident: IncidentItem) {
        viewModelScope.launch {
            val nextStage = when (incident.stage) {
                IncidentStage.DETECT -> IncidentStage.INVESTIGATE
                IncidentStage.INVESTIGATE -> IncidentStage.CONTAIN
                IncidentStage.CONTAIN -> IncidentStage.ERADICATE
                IncidentStage.ERADICATE -> IncidentStage.RECOVER
                IncidentStage.RECOVER -> IncidentStage.VERIFY
                IncidentStage.VERIFY -> IncidentStage.VERIFY
            }
            repository.updateIncident(incident.copy(stage = nextStage, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteIncident(id: Long) {
        viewModelScope.launch {
            repository.deleteIncident(id)
        }
    }

    // --- Reports Engine ---
    fun generateReport(title: String, scope: String, customExecutiveSummary: String = "") {
        viewModelScope.launch {
            val currentFindings = allFindings.value
            val criticalCount = currentFindings.count { it.severity == AuditSeverity.CRITICAL }
            val highCount = currentFindings.count { it.severity == AuditSeverity.HIGH }

            val execSummary = if (customExecutiveSummary.isNotBlank()) customExecutiveSummary else {
                "Dark Avatar conducted a defensive security assessment covering scope: '$scope'. A total of ${currentFindings.size} findings were identified, including $criticalCount Critical and $highCount High severity vulnerabilities requiring immediate remediation."
            }

            val mdBuilder = StringBuilder()
            mdBuilder.append("# DARK AVATAR CYBERSECURITY AUDIT REPORT\n\n")
            mdBuilder.append("**Title**: $title  \n")
            mdBuilder.append("**Scope**: $scope  \n")
            mdBuilder.append("**Generated**: ${java.util.Date()}  \n\n")
            mdBuilder.append("## Executive Summary\n")
            mdBuilder.append("$execSummary\n\n")
            mdBuilder.append("## Risk Matrix\n")
            mdBuilder.append("- Critical: $criticalCount\n")
            mdBuilder.append("- High: $highCount\n")
            mdBuilder.append("- Medium: ${currentFindings.count { it.severity == AuditSeverity.MEDIUM }}\n")
            mdBuilder.append("- Low/Info: ${currentFindings.count { it.severity == AuditSeverity.LOW || it.severity == AuditSeverity.INFO }}\n\n")
            mdBuilder.append("## Findings & Remediation\n\n")

            for ((idx, f) in currentFindings.withIndex()) {
                mdBuilder.append("### ${idx + 1}. [${f.severity}] ${f.title}\n")
                mdBuilder.append("- **Category**: ${f.category}\n")
                mdBuilder.append("- **Location**: ${f.location}\n")
                mdBuilder.append("- **Problem**: ${f.problem}\n")
                mdBuilder.append("- **Impact**: ${f.whyItMatters}\n")
                mdBuilder.append("- **Remediation**: ${f.secureFix}\n")
                if (f.improvedCode.isNotBlank()) {
                    mdBuilder.append("```\n${f.improvedCode}\n```\n")
                }
                mdBuilder.append("\n---\n\n")
            }

            val jsonOutput = JSONObject().apply {
                put("title", title)
                put("scope", scope)
                put("executiveSummary", execSummary)
                put("criticalCount", criticalCount)
                put("highCount", highCount)
                put("timestamp", System.currentTimeMillis())
                put("findings", JSONArray().apply {
                    for (f in currentFindings) {
                        put(JSONObject().apply {
                            put("title", f.title)
                            put("severity", f.severity.name)
                            put("category", f.category)
                            put("problem", f.problem)
                            put("secureFix", f.secureFix)
                        })
                    }
                })
            }

            val newReport = ReportItem(
                title = title,
                scope = scope,
                executiveSummary = execSummary,
                findingsCount = currentFindings.size,
                criticalCount = criticalCount,
                highCount = highCount,
                contentMarkdown = mdBuilder.toString(),
                contentJson = jsonOutput.toString(2)
            )

            repository.insertReport(newReport)
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    // --- Interactive Cyber Terminal Engine ---
    fun executeTerminalCommand(cmdText: String) {
        val trimmed = cmdText.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val parts = trimmed.split(Regex("\\s+"))
            val command = parts[0].lowercase()

            val output: String
            var isError = false

            when {
                command == "help" || command == "?" || command == "commands" -> {
                    output = """
DARK AVATAR CYBER COMMAND WORKSTATION v2.5

AVAILABLE COMMANDS:
  help                              - Show this command reference
  analyze logs <log_text>           - Parse auth/system log for brute force & anomalies
  audit code <lang> <code_snippet>  - Run security audit on code snippet
  inspect hash <hash_string>        - Identify algorithm & calculate hashes
  inspect network <host> <port>     - Real socket port connection probe
  dns lookup <domain>               - Perform DNS query on target host
  http headers <url>                - Audit HTTP/TLS security headers (CSP, HSTS)
  analyze apk <manifest_xml>        - Audit AndroidManifest.xml for vulnerabilities
  harden system <linux|ssh|nginx>   - Output hardening configurations & rules
  incident create <title>           - Spawn a new incident response workflow
  incident list                     - List active incidents and stages
  generate report <title>           - Compile active findings into audit report
  status                            - Display workstation telemetry & score
  clear                             - Clear terminal history
"""
                }
                command == "clear" -> {
                    repository.clearTerminalHistory()
                    return@launch
                }
                command == "status" -> {
                    val m = metrics.value
                    output = """
[DARK AVATAR TELEMETRY]
SECURITY POSTURE SCORE: ${m.securityScore}/100
CRITICAL FINDINGS:      ${m.criticalFindings}
HIGH RISK FINDINGS:     ${m.highFindings}
TOTAL FINDINGS SAVED:   ${m.totalFindings}
ACTIVE INCIDENTS:       ${m.activeIncidents}
REPORTS GENERATED:      ${m.reportsGenerated}
SYSTEM ENGINE:          ONLINE (Local Heuristics + Gemini Cyber Copilot)
"""
                }
                command == "inspect" && parts.size >= 3 && parts[1].lowercase() == "hash" -> {
                    val hashArg = parts.drop(2).joinToString(" ")
                    val identified = OfflineSecurityEngine.identifyHashType(hashArg)
                    val hashes = OfflineSecurityEngine.calculateHashes(hashArg)
                    val entropy = OfflineSecurityEngine.calculateEntropy(hashArg)
                    output = """
[HASH INSPECTION: $hashArg]
IDENTIFIED TYPE: ${identified.joinToString(", ")}
ENTROPY:         ${entropy["entropy"]} (${entropy["strength"]})
CRACK ESTIMATE:  ${entropy["crackTime"]}

HASH EQUIVALENTS (OF INPUT STRING):
MD5:     ${hashes["MD5"]}
SHA-1:   ${hashes["SHA-1"]}
SHA-256: ${hashes["SHA-256"]}
SHA-512: ${hashes["SHA-512"]}
"""
                }
                command == "dns" && parts.size >= 3 && parts[1].lowercase() == "lookup" -> {
                    val domain = parts[2]
                    val dns = OfflineSecurityEngine.resolveDns(domain)
                    output = "[DNS RESOLUTION: $domain]\n" + dns.joinToString("\n") { " -> $it" }
                }
                command == "http" && parts.size >= 3 && parts[1].lowercase() == "headers" -> {
                    val url = parts[2]
                    val headers = OfflineSecurityEngine.inspectHttpHeaders(url)
                    output = if (headers.containsKey("error")) {
                        isError = true
                        headers["error"].toString()
                    } else {
                        val present = headers["presentHeaders"] as? Map<*, *> ?: emptyMap<Any, Any>()
                        val missing = headers["missingHeaders"] as? List<*> ?: emptyList<Any>()
                        """
[HTTP SECURITY AUDIT: $url]
STATUS: ${headers["status"]}
SERVER: ${headers["server"]}

PRESENT SECURITY HEADERS (${present.size}):
${present.entries.joinToString("\n") { " [+] ${it.key}: ${it.value}" }}

MISSING SECURITY HEADERS (${missing.size}):
${missing.joinToString("\n") { " [-] $it (NOT CONFIGURED)" }}
"""
                    }
                }
                command == "inspect" && parts.size >= 4 && parts[1].lowercase() == "network" -> {
                    val host = parts[2]
                    val port = parts[3].toIntOrNull() ?: 80
                    val (isOpen, desc) = OfflineSecurityEngine.probePort(host, port)
                    output = "[NETWORK SOCKET PROBE: $host:$port]\nSTATUS: " + if (isOpen) "OPEN" else "CLOSED" + "\nDETAILS: $desc"
                }
                command == "analyze" && parts.size >= 3 && parts[1].lowercase() == "logs" -> {
                    val logText = parts.drop(2).joinToString(" ")
                    val parsed = OfflineSecurityEngine.parseSecurityLogs(logText)
                    output = """
[LOG ANALYSIS ENGINE]
THREAT SCORE:         ${parsed["threatScore"]}
FAILED AUTH ATTEMPTS: ${parsed["failedLoginCount"]}
SQL INJECTION PROBES: ${parsed["sqliCount"]}
UNIQUE IP ADDRESSES:  ${parsed["uniqueIps"]}
TOP IPS:
${(parsed["topIps"] as? List<*>)?.joinToString("\n") { " • $it" } ?: "None"}
"""
                }
                command == "analyze" && parts.size >= 3 && parts[1].lowercase() == "apk" -> {
                    val xml = parts.drop(2).joinToString(" ")
                    val findings = OfflineSecurityEngine.auditAndroidManifest(xml)
                    output = "[APK MANIFEST AUDIT]\nFOUND ${findings.size} FINDINGS:\n" +
                            findings.joinToString("\n\n") { "[${it.severity}] ${it.title}\n ${it.problem}\n FIX: ${it.secureFix}" }
                }
                command == "harden" && parts.size >= 3 && parts[1].lowercase() == "system" -> {
                    val target = parts[2].lowercase()
                    output = when (target) {
                        "ssh" -> """
# SECURE /etc/ssh/sshd_config
Port 2222
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
X11Forwarding no
MaxAuthTries 3
ClientAliveInterval 300
ClientAliveCountMax 2
"""
                        "nginx" -> """
# SECURE NGINX HEADERS & TLS
ssl_protocols TLSv1.2 TLSv1.3;
ssl_prefer_server_ciphers on;
add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
add_header X-Frame-Options "DENY" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Content-Security-Policy "default-src 'self';" always;
server_tokens off;
"""
                        "docker" -> """
# SECURE DOCKER RUNTIME
docker run -d \
  --read-only \
  --cap-drop=ALL \
  --cap-add=NET_BIND_SERVICE \
  --security-opt=no-new-privileges:true \
  --user 10001:10001 \
  --name secure_app app:latest
"""
                        else -> """
# LINUX KERNEL SYSCTL HARDENING (/etc/sysctl.d/99-security.conf)
net.ipv4.tcp_syncookies = 1
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.all.send_redirects = 0
net.ipv4.conf.all.accept_source_route = 0
kernel.randomize_va_space = 2
fs.protected_hardlinks = 1
fs.protected_symlinks = 1
"""
                    }
                }
                command == "incident" && parts.size >= 2 -> {
                    when (parts[1].lowercase()) {
                        "list" -> {
                            val incs = allIncidents.value
                            output = if (incs.isEmpty()) "No active incidents recorded." else {
                                "[ACTIVE INCIDENTS]\n" + incs.joinToString("\n") { "• [#${it.id}] [STAGE: ${it.stage}] [${it.severity}] ${it.title}" }
                            }
                        }
                        "create" -> {
                            val title = parts.drop(2).joinToString(" ").ifBlank { "Untitled Incident" }
                            createIncident(title, "Incident initiated via terminal workstation", AuditSeverity.HIGH)
                            output = "[INCIDENT CREATED]: '$title' -> Initialized at DETECT stage."
                        }
                        else -> {
                            output = "Usage: incident [list|create <title>]"
                            isError = true
                        }
                    }
                }
                command == "generate" && parts.size >= 3 && parts[1].lowercase() == "report" -> {
                    val title = parts.drop(2).joinToString(" ").ifBlank { "Security Assessment" }
                    generateReport(title, "Workstation Active Scope")
                    output = "[REPORT GENERATED]: '$title' added to Reports repository."
                }
                else -> {
                    output = "Command not recognized: '$trimmed'. Type 'help' for valid security commands."
                    isError = true
                }
            }

            repository.logTerminalCommand(trimmed, output, isError)
        }
    }

    fun purgeAllData() {
        viewModelScope.launch {
            repository.clearFindings()
            repository.clearReports()
            repository.clearIncidents()
            repository.clearTerminalHistory()
            _activeAuditFindings.value = emptyList()
            _manifestFindings.value = emptyList()
            _logAnalysisResult.value = null
            _malwareAnalysisResult.value = null
            _chatMessages.value = listOf(
                ChatMessage(
                    isUser = false,
                    content = "DARK AVATAR DATABASE PURGED. ALL LOCAL DATA WIPED SECURELY.",
                    mode = "SYSTEM_PURGE"
                )
            )
        }
    }
}
