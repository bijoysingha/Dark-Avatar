package com.example.data.model

enum class AuditSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO
}

enum class IncidentStage {
    DETECT,
    INVESTIGATE,
    CONTAIN,
    ERADICATE,
    RECOVER,
    VERIFY
}

enum class CodeLanguage(val displayName: String, val extension: String, val sampleSnippet: String) {
    PYTHON("Python", "py", "import socket\n\ndef scan_port(ip, port):\n    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)\n    s.settimeout(1.0)\n    return s.connect_ex((ip, port)) == 0"),
    BASH("Bash", "sh", "#!/usr/bin/env bash\nset -euo pipefail\n\n# Audit open listening ports\nnetstat -tuln | awk 'NR>2 {print \$4}' | cut -d: -f2 | sort -nu"),
    KOTLIN("Kotlin", "kt", "fun verifySha256(data: ByteArray, expectedHash: String): Boolean {\n    val md = MessageDigest.getInstance(\"SHA-256\")\n    val digest = md.digest(data).joinToString(\"\") { \"%02x\".format(it) }\n    return digest.equals(expectedHash, ignoreCase = true)\n}"),
    JAVA("Java", "java", "public static boolean isValidHost(String host) {\n    return host != null && host.matches(\"^[a-zA-Z0-9.-]+$\");\n}"),
    JAVASCRIPT("JavaScript", "js", "const crypto = require('crypto');\n\nfunction sanitizeHeader(val) {\n  return String(val).replace(/[\\r\\n]/g, '');\n}"),
    C("C", "c", "#include <stdio.h>\n#include <string.h>\n\nvoid safe_copy(char *dest, size_t dest_sz, const char *src) {\n    snprintf(dest, dest_sz, \"%s\", src);\n}"),
    CPP("C++", "cpp", "#include <iostream>\n#include <string_view>\n\nbool is_secure_channel(std::string_view protocol) {\n    return protocol == \"tls1.3\";\n}"),
    POWERSHELL("PowerShell", "ps1", "# Query active defensive firewall rules\nGet-NetFirewallRule -Enabled True | Where-Object Direction -eq 'Inbound' | Select-Object DisplayName, Action"),
    SQL("SQL", "sql", "-- Parameterized prepared query template\nSELECT user_id, role, last_login \nFROM sys_audit_users \nWHERE username = ? AND status = 'ACTIVE';"),
    HTML("HTML", "html", "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'self'; script-src 'self'; object-src 'none';\">"),
    CSS("CSS", "css", "/* High-contrast accessible cyber UI tokens */\n:root {\n  --cyber-primary: #00f0ff;\n  --cyber-bg: #070a0f;\n}")
}

enum class CodeAuditMode {
    GENERATE,
    EXPLAIN,
    DEBUG,
    AUDIT,
    OPTIMIZE,
    SECURE
}

data class FindingItem(
    val id: Long = 0,
    val title: String,
    val category: String,
    val severity: AuditSeverity,
    val location: String = "N/A",
    val problem: String,
    val whyItMatters: String,
    val secureFix: String,
    val improvedCode: String = "",
    val rawEvidence: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class IncidentItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val stage: IncidentStage = IncidentStage.DETECT,
    val severity: AuditSeverity = AuditSeverity.HIGH,
    val affectedSystems: String = "",
    val iocs: String = "",
    val containmentActions: String = "",
    val recoveryPlan: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ReportItem(
    val id: Long = 0,
    val title: String,
    val scope: String,
    val executiveSummary: String,
    val findingsCount: Int = 0,
    val criticalCount: Int = 0,
    val highCount: Int = 0,
    val contentMarkdown: String,
    val contentJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis()
)

data class TerminalHistoryItem(
    val id: Long = 0,
    val command: String,
    val output: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CyberMetrics(
    val securityScore: Int = 88,
    val totalFindings: Int = 0,
    val criticalFindings: Int = 0,
    val highFindings: Int = 0,
    val activeIncidents: Int = 0,
    val reportsGenerated: Int = 0
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val content: String,
    val mode: String = "DEFENSIVE_CODER",
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val structuredToolData: String? = null
)
