package com.example.ai

import com.example.data.model.AuditSeverity
import com.example.data.model.CodeLanguage
import com.example.data.model.FindingItem
import com.example.data.model.IncidentItem
import com.example.data.model.IncidentStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.security.MessageDigest
import java.util.regex.Pattern
import kotlin.math.log2

object OfflineSecurityEngine {

    // --- 1. Real Cryptographic Hashing ---
    fun calculateHashes(input: String): Map<String, String> {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return mapOf(
            "MD5" to hashBytes(bytes, "MD5"),
            "SHA-1" to hashBytes(bytes, "SHA-1"),
            "SHA-256" to hashBytes(bytes, "SHA-256"),
            "SHA-512" to hashBytes(bytes, "SHA-512")
        )
    }

    private fun hashBytes(bytes: ByteArray, algorithm: String): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            val digest = md.digest(bytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "ERR: ${e.message}"
        }
    }

    // Hash Identifier
    fun identifyHashType(hash: String): List<String> {
        val trimmed = hash.trim()
        val len = trimmed.length
        val isHex = trimmed.matches(Regex("^[a-fA-F0-9]+$"))

        val candidates = mutableListOf<String>()
        when {
            trimmed.startsWith("\$2a\$") || trimmed.startsWith("\$2b\$") || trimmed.startsWith("\$2y\$") -> candidates.add("bcrypt Blowfish Password Hash (Cost 4-31)")
            trimmed.startsWith("\$6\$") -> candidates.add("SHA-512 Crypt (Unix /etc/shadow)")
            trimmed.startsWith("\$5\$") -> candidates.add("SHA-256 Crypt (Unix /etc/shadow)")
            trimmed.startsWith("\$1\$") -> candidates.add("MD5 Crypt (Unix legacy)")
            trimmed.startsWith("\$argon2id\$") || trimmed.startsWith("\$argon2i\$") -> candidates.add("Argon2 Modern Password Hashing")
            len == 32 && isHex -> {
                candidates.add("MD5 (128-bit)")
                candidates.add("NTLM (Windows NT LanMan)")
                candidates.add("MD4 (128-bit)")
            }
            len == 40 && isHex -> {
                candidates.add("SHA-1 (160-bit)")
                candidates.add("RIPEMD-160")
                candidates.add("MySQL 4.1+ Hash")
            }
            len == 64 && isHex -> {
                candidates.add("SHA-256 (256-bit)")
                candidates.add("HMAC-SHA256")
                candidates.add("Keccak-256 / SHA3-256")
            }
            len == 96 && isHex -> {
                candidates.add("SHA-384 (384-bit)")
            }
            len == 128 && isHex -> {
                candidates.add("SHA-512 (512-bit)")
                candidates.add("Whirlpool (512-bit)")
            }
            else -> candidates.add("Unknown hash format (Length: $len, Hex: $isHex)")
        }
        return candidates
    }

    // Password Entropy Calculator
    fun calculateEntropy(password: String): Map<String, Any> {
        if (password.isEmpty()) {
            return mapOf("entropy" to 0.0, "strength" to "EMPTY", "poolSize" to 0, "crackTime" to "Instant")
        }
        var pool = 0
        if (password.any { it.isLowerCase() }) pool += 26
        if (password.any { it.isUpperCase() }) pool += 26
        if (password.any { it.isDigit() }) pool += 10
        if (password.any { !it.isLetterOrDigit() }) pool += 33

        val entropy = if (pool > 0) password.length * log2(pool.toDouble()) else 0.0
        val strength = when {
            entropy < 28 -> "VERY WEAK"
            entropy < 45 -> "WEAK"
            entropy < 60 -> "REASONABLE"
            entropy < 80 -> "STRONG"
            else -> "VERY STRONG / MILITARY GRADE"
        }
        val crackTime = when {
            entropy < 28 -> "< 1 millisecond (GPU Dictionary)"
            entropy < 40 -> "< 10 seconds"
            entropy < 55 -> "A few hours to 3 days"
            entropy < 70 -> "~ 10 to 500 years"
            else -> "Millions of centuries against quantum brute force"
        }

        return mapOf(
            "entropy" to String.format("%.1f bits", entropy),
            "strength" to strength,
            "poolSize" to pool,
            "crackTime" to crackTime
        )
    }

    // --- 2. Log Analysis & Anomaly Extraction ---
    fun parseSecurityLogs(logText: String): Map<String, Any> {
        val lines = logText.lines().filter { it.isNotBlank() }
        val ipPattern = Pattern.compile("(\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b)")
        val failedLoginPattern = Pattern.compile("(Failed password|authentication failure|Invalid user|AUTH_FAILED|Login failed|401 Unauthorized)", Pattern.CASE_INSENSITIVE)
        val sqlInjectionPattern = Pattern.compile("(UNION SELECT|1=1|OR '1'='1|DROP TABLE|information_schema|--|SLEEP\\(\\d+\\))", Pattern.CASE_INSENSITIVE)
        val xssPattern = Pattern.compile("(<script|javascript:|alert\\(|onload=|onerror=)", Pattern.CASE_INSENSITIVE)
        val sudoPattern = Pattern.compile("(sudo:|COMMAND=|pam_unix\\(sudo:auth\\))", Pattern.CASE_INSENSITIVE)

        val ipCounts = mutableMapOf<String, Int>()
        val failedLogins = mutableListOf<String>()
        val suspiciousEvents = mutableListOf<String>()
        var sqliDetected = 0
        var xssDetected = 0
        var sudoEvents = 0

        for (line in lines) {
            val matcher = ipPattern.matcher(line)
            while (matcher.find()) {
                val ip = matcher.group(1) ?: continue
                ipCounts[ip] = (ipCounts[ip] ?: 0) + 1
            }
            if (failedLoginPattern.matcher(line).find()) {
                failedLogins.add(line)
            }
            if (sqlInjectionPattern.matcher(line).find()) {
                sqliDetected++
                suspiciousEvents.add("[SQL INJECTION PROBE] $line")
            }
            if (xssPattern.matcher(line).find()) {
                xssDetected++
                suspiciousEvents.add("[XSS PAYLOAD DETECTED] $line")
            }
            if (sudoPattern.matcher(line).find()) {
                sudoEvents++
            }
        }

        val topIps = ipCounts.toList().sortedByDescending { it.second }.take(5)
        val bruteForceSuspects = ipCounts.filter { it.value >= 3 }.keys.toList()

        val threatScore = when {
            sqliDetected > 0 || bruteForceSuspects.isNotEmpty() -> "HIGH / ACTIVE ATTACK"
            failedLogins.size > 2 -> "ELEVATED"
            else -> "LOW / ROUTINE"
        }

        return mapOf(
            "totalLines" to lines.size,
            "threatScore" to threatScore,
            "uniqueIps" to ipCounts.size,
            "topIps" to topIps,
            "failedLoginCount" to failedLogins.size,
            "bruteForceSuspects" to bruteForceSuspects,
            "sqliCount" to sqliDetected,
            "xssCount" to xssDetected,
            "sudoEvents" to sudoEvents,
            "suspiciousEvents" to suspiciousEvents
        )
    }

    // --- 3. Static Code Security Audit Rules ---
    fun auditCodeLocally(code: String, language: CodeLanguage): List<FindingItem> {
        val findings = mutableListOf<FindingItem>()

        // Rule 1: Hardcoded Secrets & API Keys
        val secretRegex = Regex("(?i)(api[_-]?key|secret|password|passwd|auth[_-]?token|private[_-]?key)\\s*[:=]\\s*[\"']([A-Za-z0-9_\\-\\+=]{6,})[\"']")
        val secretMatch = secretRegex.find(code)
        if (secretMatch != null) {
            findings.add(
                FindingItem(
                    title = "Hardcoded Secret / Credential Detected",
                    category = "CWE-798: Use of Hardcoded Credentials",
                    severity = AuditSeverity.CRITICAL,
                    location = "Line matching '${secretMatch.value.take(40)}...'",
                    problem = "Sensitive API key or credential is hardcoded directly in the source code.",
                    whyItMatters = "Attackers decompiling binaries or inspecting code repositories can immediately extract and abuse credentials.",
                    secureFix = "Inject credentials via environment variables, encrypted key stores, or secure vaults.",
                    improvedCode = "// Injected securely via environment or BuildConfig\nval apiKey = System.getenv(\"API_KEY\") ?: BuildConfig.API_KEY"
                )
            )
        }

        // Rule 2: SQL Injection
        val sqliRegex = Regex("(?i)(SELECT|UPDATE|DELETE|INSERT).*(\\+\\s*[a-zA-Z0-9_]+|\\$\\s*\\{|%s|format\\()")
        if (sqliRegex.containsMatchIn(code)) {
            findings.add(
                FindingItem(
                    title = "Potential SQL Injection Flaw",
                    category = "CWE-89: SQL Injection",
                    severity = AuditSeverity.CRITICAL,
                    location = "Dynamic query concatenation statement",
                    problem = "Unsanitized user input is concatenated directly into a database query string.",
                    whyItMatters = "Enables unauthorized database enumeration, authentication bypass, and potential data exfiltration or table destruction.",
                    secureFix = "Use parameterized prepared statements with positional parameters (?) or ORM binding.",
                    improvedCode = "val stmt = connection.prepareStatement(\"SELECT * FROM users WHERE id = ?\")\nstmt.setString(1, userId)\nval rs = stmt.executeQuery()"
                )
            )
        }

        // Rule 3: Command Injection
        val cmdRegex = Regex("(?i)(Runtime\\.getRuntime\\(\\)\\.exec|ProcessBuilder|os\\.system|subprocess\\.Popen\\(.*shell=True|exec\\(|system\\()")
        if (cmdRegex.containsMatchIn(code)) {
            findings.add(
                FindingItem(
                    title = "Unsafe Command Execution / Shell Injection",
                    category = "CWE-78: OS Command Injection",
                    severity = AuditSeverity.HIGH,
                    location = "Native execution invocation",
                    problem = "Passing shell strings to native exec or shell=True allows command chaining (e.g., ; rm -rf /).",
                    whyItMatters = "Allows an attacker who controls the argument to achieve arbitrary Remote Code Execution (RCE) on the host.",
                    secureFix = "Pass arguments as structured lists without shell interpolation, or use native API libraries instead of spawning shell subshells.",
                    improvedCode = "// Pass arguments as array/list without shell=True\nval pb = ProcessBuilder(listOf(\"/usr/bin/ping\", \"-c\", \"4\", sanitizedTarget))"
                )
            )
        }

        // Rule 4: Weak Cryptography (MD5 / SHA1)
        val weakCryptoRegex = Regex("(?i)(MessageDigest\\.getInstance\\([\"'](MD5|SHA-1|DES)[\"']|hashlib\\.md5|crypto\\.createHash\\([\"']md5[\"']\\))")
        if (weakCryptoRegex.containsMatchIn(code)) {
            findings.add(
                FindingItem(
                    title = "Deprecated Weak Cryptographic Algorithm",
                    category = "CWE-327: Use of a Broken or Risky Cryptographic Algorithm",
                    severity = AuditSeverity.MEDIUM,
                    location = "Cryptographic primitive initialization",
                    problem = "MD5 and SHA-1 are cryptographically broken and vulnerable to collision attacks.",
                    whyItMatters = "Attackers can generate forged signatures and collision payloads with identical hashes.",
                    secureFix = "Upgrade to SHA-256, SHA-512, or SHA-3 for hashing; AES-256-GCM for symmetric encryption.",
                    improvedCode = "val md = MessageDigest.getInstance(\"SHA-256\")\nval digest = md.digest(data)"
                )
            )
        }

        // Rule 5: Insecure TLS / Hostname Verification Bypass
        if (code.contains("ALLOW_ALL_HOSTNAME_VERIFIER", ignoreCase = true) ||
            code.contains("TrustAllCerts", ignoreCase = true) ||
            code.contains("verify=False", ignoreCase = true) ||
            code.contains("rejectUnauthorized: false", ignoreCase = true)
        ) {
            findings.add(
                FindingItem(
                    title = "TLS Certificate Validation Disabled",
                    category = "CWE-295: Improper Certificate Validation",
                    severity = AuditSeverity.CRITICAL,
                    location = "Network / TLS configuration",
                    problem = "Certificate trust chain verification or hostname validation is explicitly bypassed.",
                    whyItMatters = "Leaves all encrypted traffic vulnerable to Man-In-The-Middle (MITM) interception and manipulation.",
                    secureFix = "Enforce standard system CA validation and implement Certificate Pinning if high security is required.",
                    improvedCode = "// Use default system trust manager with CertificatePinner\nval pinner = CertificatePinner.Builder()\n    .add(\"api.target.com\", \"sha256/XXXX...\")\n    .build()"
                )
            )
        }

        if (findings.isEmpty()) {
            findings.add(
                FindingItem(
                    title = "No High-Risk Offline Vulnerabilities Detected",
                    category = "Static Rule Inspection Passed",
                    severity = AuditSeverity.INFO,
                    location = "Source Code File",
                    problem = "Standard offline regex pattern matches found no direct hardcoded secrets, plain SQLi, or shell injections.",
                    whyItMatters = "Basic hygiene verified. Deeper logical flaw analysis can be performed with AI Copilot.",
                    secureFix = "Maintain defensive principles: input validation, least privilege, parameterization, and dependency scanning.",
                    improvedCode = "// Code follows baseline secure patterns."
                )
            )
        }

        return findings
    }

    // --- 4. Android APK Manifest Inspector ---
    fun auditAndroidManifest(manifestXml: String): List<FindingItem> {
        val findings = mutableListOf<FindingItem>()

        if (manifestXml.contains("android:debuggable=\"true\"", ignoreCase = true)) {
            findings.add(
                FindingItem(
                    title = "Debuggable Flag Enabled in Production",
                    category = "Android Security CWE-215",
                    severity = AuditSeverity.CRITICAL,
                    location = "<application android:debuggable=\"true\">",
                    problem = "App is marked as debuggable, allowing JDWP debugger attachment and memory dumping.",
                    whyItMatters = "Any local user or malware with USB debugging can attach a debugger, inspect memory, and extract runtime keys.",
                    secureFix = "Set android:debuggable=\"false\" or ensure build variants strip debug flags for release.",
                    improvedCode = "<application\n    android:debuggable=\"false\" ... />"
                )
            )
        }

        if (manifestXml.contains("android:usesCleartextTraffic=\"true\"", ignoreCase = true)) {
            findings.add(
                FindingItem(
                    title = "Cleartext HTTP Traffic Allowed",
                    category = "Android Security CWE-319",
                    severity = AuditSeverity.HIGH,
                    location = "<application android:usesCleartextTraffic=\"true\">",
                    problem = "Allows unencrypted plain HTTP communication over network interfaces.",
                    whyItMatters = "Exposes network requests to local network eavesdropping and MITM packet modification.",
                    secureFix = "Set android:usesCleartextTraffic=\"false\" and enforce HTTPS via network_security_config.xml.",
                    improvedCode = "<application\n    android:usesCleartextTraffic=\"false\"\n    android:networkSecurityConfig=\"@xml/network_security_config\" ... />"
                )
            )
        }

        if (manifestXml.contains("android:allowBackup=\"true\"", ignoreCase = true)) {
            findings.add(
                FindingItem(
                    title = "ADB Data Backup Allowed",
                    category = "Android Security CWE-200",
                    severity = AuditSeverity.MEDIUM,
                    location = "<application android:allowBackup=\"true\">",
                    problem = "ADB backup can extract local application shared preferences, databases, and cached tokens.",
                    whyItMatters = "Enables unauthorized extraction of user databases via adb backup on unlocked devices.",
                    secureFix = "Set android:allowBackup=\"false\" or configure granular data_extraction_rules.xml.",
                    improvedCode = "<application\n    android:allowBackup=\"false\"\n    android:dataExtractionRules=\"@xml/data_extraction_rules\" ... />"
                )
            )
        }

        val exportedComponentRegex = Regex("<(activity|service|receiver|provider)[^>]*android:exported=\"true\"[^>]*>", RegexOption.DOT_MATCHES_ALL)
        val exportedMatches = exportedComponentRegex.findAll(manifestXml).toList()
        for (match in exportedMatches) {
            val componentText = match.value
            if (!componentText.contains("android:permission")) {
                findings.add(
                    FindingItem(
                        title = "Unprotected Exported Component",
                        category = "Android Security CWE-926",
                        severity = AuditSeverity.HIGH,
                        location = componentText.lines().firstOrNull() ?: "Exported Component",
                        problem = "Component is exported to third-party applications without requiring a custom signature or permission.",
                        whyItMatters = "Any rogue app installed on the same device can trigger this component or inject malicious intents.",
                        secureFix = "Set android:exported=\"false\" if internal, or guard with android:permission=\"custom.permission\".",
                        improvedCode = "<activity\n    android:name=\".TargetActivity\"\n    android:exported=\"false\" />"
                    )
                )
            }
        }

        if (findings.isEmpty()) {
            findings.add(
                FindingItem(
                    title = "Android Manifest Security Baseline Passed",
                    category = "Android Manifest Audit",
                    severity = AuditSeverity.INFO,
                    location = "AndroidManifest.xml",
                    problem = "No obvious debuggable flags, cleartext traffic, or unprotected exported components found.",
                    whyItMatters = "Baseline Android configuration adheres to secure deployment rules.",
                    secureFix = "Continue verifying dynamic runtime permissions and intent filter validations.",
                    improvedCode = "<!-- Clean configuration -->"
                )
            )
        }

        return findings
    }

    // --- 5. Malware Static Analyzer ---
    fun analyzeMalwareStrings(content: String): Map<String, Any> {
        val indicators = mutableListOf<String>()
        var threatScore = 0

        // Check for suspicious APIs / commands
        val highRiskStrings = listOf(
            "/bin/sh" to "Direct shell execution string",
            "/bin/bash" to "Direct bash invocation string",
            "cmd.exe /c" to "Windows command shell invocation",
            "powershell.exe -enc" to "Encoded PowerShell command execution",
            "CreateProcess" to "Win32 process creation API",
            "VirtualAlloc" to "Direct memory allocation for shellcode execution",
            "WriteProcessMemory" to "Process injection capability",
            "ptrace(PTRACE_TRACEME" to "Anti-debugging evasion mechanism",
            "/proc/self/mem" to "In-memory binary patching technique",
            "eval(base64_decode" to "Obfuscated payload execution wrapper",
            "nc -e" to "Netcat reverse shell backdoor payload",
            "curl -s http" to "Downloader cradle string",
            "wget http" to "Remote payload retrieval string",
            "/etc/cron." to "Linux persistence mechanism via cron",
            "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run" to "Windows registry persistence key"
        )

        for ((token, desc) in highRiskStrings) {
            if (content.contains(token, ignoreCase = true)) {
                indicators.add("[!] $token — $desc")
                threatScore += 25
            }
        }

        // IP Address & URL Extraction
        val urlRegex = Regex("https?://[a-zA-Z0-9.-]+(:[0-9]+)?(/\\S*)?")
        val extractedUrls = urlRegex.findAll(content).map { it.value }.distinct().take(10).toList()
        if (extractedUrls.isNotEmpty()) {
            indicators.add("[+] Found ${extractedUrls.size} embedded URLs/endpoints")
        }

        val threatLevel = when {
            threatScore >= 50 -> "CRITICAL / MALICIOUS INDICATORS CONFIRMED"
            threatScore >= 25 -> "SUSPICIOUS / ELEVATED RISK"
            threatScore > 0 -> "MEDIUM / POTENTIALLY UNWANTED OR DUAL-USE"
            else -> "LOW / NO IMMEDIATE STATIC SIGNATURE DETECTED"
        }

        return mapOf(
            "threatLevel" to threatLevel,
            "score" to threatScore.coerceAtMost(100),
            "indicators" to indicators,
            "embeddedUrls" to extractedUrls,
            "containment" to if (threatScore > 0) listOf(
                "Isolate suspicious host/process from local network immediately.",
                "Capture process memory dump and file hashes for threat intelligence cross-referencing.",
                "Block identified network indicators (IPs/domains) on perimeter firewall.",
                "Revoke and rotate any API credentials or certificates present in the environment."
            ) else listOf("No active containment required. Maintain monitoring.")
        )
    }

    // --- 6. Real Authorized Network Prober & HTTP Security Header Checker ---
    suspend fun probePort(host: String, port: Int, timeoutMs: Int = 1200): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                Pair(true, "Port $port is OPEN (Connected successfully)")
            }
        } catch (e: Exception) {
            Pair(false, "Port $port is CLOSED/FILTERED (${e.message ?: "Timeout"})")
        }
    }

    suspend fun resolveDns(domain: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val addresses = InetAddress.getAllByName(domain)
            addresses.map { "${it.hostName} -> ${it.hostAddress}" }
        } catch (e: Exception) {
            listOf("DNS Lookup Failed: ${e.message}")
        }
    }

    suspend fun inspectHttpHeaders(urlString: String): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val fixedUrl = if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                "https://$urlString"
            } else urlString

            val url = URL(fixedUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "HEAD"
                setRequestProperty("User-Agent", "DarkAvatar-CyberSecAudit/1.0")
            }
            conn.connect()

            val headers = conn.headerFields
            val status = conn.responseCode

            val securityHeadersToCheck = listOf(
                "Strict-Transport-Security",
                "Content-Security-Policy",
                "X-Frame-Options",
                "X-Content-Type-Options",
                "Referrer-Policy",
                "Permissions-Policy"
            )

            val presentHeaders = mutableMapOf<String, String>()
            val missingHeaders = mutableListOf<String>()

            for (h in securityHeadersToCheck) {
                val value = headers[h]?.firstOrNull() ?: headers[h.lowercase()]?.firstOrNull()
                if (value != null) {
                    presentHeaders[h] = value
                } else {
                    missingHeaders.add(h)
                }
            }

            mapOf(
                "status" to status,
                "url" to fixedUrl,
                "presentHeaders" to presentHeaders,
                "missingHeaders" to missingHeaders,
                "server" to (headers["Server"]?.firstOrNull() ?: "Hidden/Not Disclosed")
            )
        } catch (e: Exception) {
            mapOf("error" to "HTTP Header Inspection Error: ${e.message}")
        }
    }
}
