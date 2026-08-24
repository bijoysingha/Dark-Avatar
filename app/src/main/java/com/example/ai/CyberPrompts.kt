package com.example.ai

object CyberPrompts {
    const val SYSTEM_INSTRUCTION = """
You are DARK AVATAR — an advanced, direct, fast AI cybersecurity workstation inspired by the practical security tool assistance of Kali Linux and the technical coding and automation capabilities of specialized security AI.

Core Directives:
1. FOCUS: Defensive cybersecurity, authorized penetration testing, security research, vulnerability analysis, secure coding, log forensics, malware static analysis, system hardening, and automated incident response.
2. TONE: Direct, highly technical, concise, authoritative, and actionable. No conversational fluff or disclaimers.
3. ACCURACY: Never claim a command or scan was executed unless provided with real tool results.
4. SCOPE DISCIPLINE: Provide defensive and authorized laboratory guidance. If a query touches risky offensive actions, provide the authorized lab testing or defensive mitigation alternative directly.
5. FORMATTING:
   - For code: Provide complete, clean, production-ready code with safe parameterization, error handling, and requirements.
   - For audits: Format findings with:
     [SEVERITY: CRITICAL | HIGH | MEDIUM | LOW]
     LOCATION:
     PROBLEM:
     WHY IT MATTERS:
     SECURE FIX:
     IMPROVED CODE:
   - For Incident Response: Follow the 6-stage workflow: DETECT -> INVESTIGATE -> CONTAIN -> ERADICATE -> RECOVER -> VERIFY.
"""

    fun buildCodeAuditPrompt(code: String, language: String, mode: String): String {
        return """
[MODE: $mode]
[LANGUAGE: $language]
Analyze or generate cybersecurity code for the following snippet or prompt:

```$language
$code
```

Provide technical, direct cybersecurity analysis including:
1. Core security posture and potential flaws (CWE / OWASP categories).
2. Severity classification (CRITICAL, HIGH, MEDIUM, LOW).
3. Exact vulnerability location, why it matters, and exploit vectors in authorized testing.
4. Corrected, hardened, and optimized production-ready secure code.
"""
    }

    fun buildLogAnalysisPrompt(logs: String): String {
        return """
Analyze the following security and authentication logs:

```
$logs
```

Execute the workflow:
READ LOG -> PARSE EVENTS -> IDENTIFY ANOMALIES -> CLASSIFY RISK -> GENERATE ACTIONABLE DEFENSE

Extract:
- Suspected brute force / credential stuffing patterns
- Originating IP addresses and anomaly counts
- Injection payloads or unauthorized command attempts
- Immediate containment steps and firewall / fail2ban rules to mitigate.
"""
    }

    fun buildMalwareAnalysisPrompt(content: String): String {
        return """
Perform static analysis on the following code snippet, disassembled strings, or configuration artifact:

```
$content
```

Output:
- THREAT LEVEL (CRITICAL / HIGH / MEDIUM / LOW / CLEAN)
- INDICATORS (Suspicious API calls, obfuscation, network endpoints, persistence)
- BEHAVIOR & CAPABILITIES
- POSSIBLE IMPACT
- CONTAINMENT STEPS
- REMEDIATION & DETECTION SIGNATURE (YARA / Snort / Sigma rule recommendation)
"""
    }

    fun buildIncidentResponsePrompt(incidentDetails: String): String {
        return """
Generate an Incident Response Playbook and timeline for the following cybersecurity scenario:

$incidentDetails

Structure using the 6-stage lifecycle:
1. DETECT: Initial alerts, IOCs, and triage criteria.
2. INVESTIGATE: Forensics evidence to collect, logs to preserve.
3. CONTAIN: Immediate network isolation, credential revocation, and process killing commands.
4. ERADICATE: Root cause remediation, malware removal, backdoor scanning.
5. RECOVER: Safe system restoration, integrity verification.
6. VERIFY: Post-incident monitoring and lessons learned.
"""
    }

    fun buildSystemHardeningPrompt(targetSystem: String, requirements: String): String {
        return """
Generate a comprehensive system hardening configuration and script for:
Target System: $targetSystem
Requirements: $requirements

Provide actionable configuration files (e.g. sshd_config, sysctl.conf, ufw, auditd, docker-compose.yml) and hardening commands with exact explanations of each security parameter.
"""
    }
}
