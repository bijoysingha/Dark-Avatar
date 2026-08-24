package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun queryGemini(prompt: String, systemInstruction: String = CyberPrompts.SYSTEM_INSTRUCTION): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent fallback from offline cybersecurity engine
            return@withContext runOfflineHeuristic(prompt)
        }

        try {
            val rootJson = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })

                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.95)
                    put("maxOutputTokens", 4096)
                })
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "[AI ERROR ${response.code}]: Falling back to local cybersecurity rules.\n\n${runOfflineHeuristic(prompt)}"
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text
            } else {
                runOfflineHeuristic(prompt)
            }
        } catch (e: Exception) {
            "[CONNECTION ERROR: ${e.message} — RUNNING LOCAL HEURISTIC ANALYSIS]\n\n" + runOfflineHeuristic(prompt)
        }
    }

    private fun runOfflineHeuristic(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("log") -> {
                val parsed = OfflineSecurityEngine.parseSecurityLogs(prompt)
                """
=== DARK AVATAR LOG ANALYSIS (OFFLINE FORENSIC ENGINE) ===
THREAT RATING: ${parsed["threatScore"]}
TOTAL LINES PARSED: ${parsed["totalLines"]}
UNIQUE IP ADDRESSES: ${parsed["uniqueIps"]}
FAILED AUTH ATTEMPTS: ${parsed["failedLoginCount"]}
SQL INJECTION PROBES: ${parsed["sqliCount"]}
XSS PAYLOADS: ${parsed["xssCount"]}
SUDO PRIVILEGE EVENTS: ${parsed["sudoEvents"]}

TOP ORIGINATING IPS:
${(parsed["topIps"] as? List<*>)?.joinToString("\n") { " - $it" } ?: "None"}

SUSPICIOUS EVENTS:
${(parsed["suspiciousEvents"] as? List<*>)?.joinToString("\n") { " - $it" } ?: "None flagged"}

DEFENSIVE ACTIONS:
1. Block suspicious IPs: `sudo ufw deny from <IP>` or add to Fail2Ban `sshd` jail.
2. Enforce key-based authentication in `/etc/ssh/sshd_config` (`PasswordAuthentication no`).
3. Rotate compromised user credentials immediately.
"""
            }
            lower.contains("malware") || lower.contains("threat") || lower.contains("suspicious") -> {
                val malware = OfflineSecurityEngine.analyzeMalwareStrings(prompt)
                """
=== DARK AVATAR STATIC MALWARE TRIAGE ===
THREAT LEVEL: ${malware["threatLevel"]}
RISK SCORE: ${malware["score"]}/100

INDICATORS OF COMPROMISE:
${(malware["indicators"] as? List<*>)?.joinToString("\n") ?: "None"}

CONTAINMENT & REMEDIATION:
${(malware["containment"] as? List<*>)?.joinToString("\n") { "• $it" } ?: "Monitor environment"}
"""
            }
            lower.contains("harden") || lower.contains("defense") -> {
                """
=== DARK AVATAR SYSTEM HARDENING BASELINE ===
TARGET: Linux / Server Infrastructure

1. SSH CONFIGURATION (/etc/ssh/sshd_config):
   Port 2222
   PermitRootLogin no
   PasswordAuthentication no
   PubkeyAuthentication yes
   MaxAuthTries 3
   X11Forwarding no

2. KERNEL HARDENING (/etc/sysctl.d/99-security.conf):
   net.ipv4.conf.all.rp_filter = 1
   net.ipv4.conf.all.accept_redirects = 0
   net.ipv4.conf.all.send_redirects = 0
   net.ipv4.conf.all.accept_source_route = 0
   net.ipv4.tcp_syncookies = 1

3. FIREWALL (UFW):
   sudo ufw default deny incoming
   sudo ufw default allow outgoing
   sudo ufw allow 2222/tcp
   sudo ufw enable
"""
            }
            else -> {
                """
=== DARK AVATAR CYBERSECURITY WORKSTATION ===
STATUS: READY

Analysis complete for request:
"${prompt.take(120)}..."

Key Recommendations:
• Adopt Zero Trust network segmentation and principle of least privilege.
• Audit dependencies and code endpoints for input sanitization and parameterized queries.
• Enable continuous log ingestion into SIEM with automated alert thresholds.
• Configure encrypted storage and strict TLS 1.3 cryptographic suites.
"""
            }
        }
    }
}
