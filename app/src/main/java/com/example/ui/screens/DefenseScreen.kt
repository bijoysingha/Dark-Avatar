package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ActionPill
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderMuted
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSky
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

data class HardeningModule(
    val title: String,
    val category: String,
    val description: String,
    val targetFile: String,
    val code: String,
    val language: String
)

@Composable
fun DefenseScreen() {
    var selectedCategory by remember { mutableStateOf("LINUX_SYSCTL") }

    val modules = listOf(
        HardeningModule(
            title = "Kernel Network & Memory Security",
            category = "LINUX_SYSCTL",
            description = "Protects against SYN flood attacks, spoofed routing packets, ICMP redirects, and enforces memory ASLR.",
            targetFile = "/etc/sysctl.d/99-security-hardening.conf",
            language = "CONF",
            code = """# Dark Avatar Kernel Hardening Configuration
# Enable SYN Flood Protection
net.ipv4.tcp_syncookies = 1

# Ignore ICMP Broadcast Requests
net.ipv4.icmp_echo_ignore_broadcasts = 1

# Disable Source Packet Routing & Redirects
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.default.accept_source_route = 0
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
net.ipv4.conf.all.send_redirects = 0

# Enable Reverse Path Filtering (Spoof Protection)
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.default.rp_filter = 1

# Enforce ASLR & Symlink Protection
kernel.randomize_va_space = 2
fs.protected_hardlinks = 1
fs.protected_symlinks = 1
fs.protected_fifos = 2
fs.protected_regular = 2
"""
        ),
        HardeningModule(
            title = "Hardened OpenSSH Daemon Configuration",
            category = "SSH_HARDENING",
            description = "Enforces ed25519 cryptography, disables password auth & root logins, and limits connection attempts.",
            targetFile = "/etc/ssh/sshd_config.d/99-hardened.conf",
            language = "CONF",
            code = """# Dark Avatar SSH Hardening Standard
Port 2222
Protocol 2
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
AuthenticationMethods publickey
MaxAuthTries 3
MaxSessions 2
ClientAliveInterval 300
ClientAliveCountMax 2
X11Forwarding no
AllowAgentForwarding no
PermitUserEnvironment no

# Strong Modern Cryptographic Ciphers
KexAlgorithms curve25519-sha256,curve25519-sha256@libssh.org,diffie-hellman-group16-sha512
Ciphers chacha20-poly1305@openssh.com,aes256-gcm@openssh.com
MACs hmac-sha2-512-etm@openssh.com,hmac-sha2-256-etm@openssh.com
"""
        ),
        HardeningModule(
            title = "Nginx TLS 1.3 & HTTP Security Headers",
            category = "NGINX_TLS",
            description = "Enforces Strict-Transport-Security (HSTS), Content-Security-Policy (CSP), and hides server disclosure tokens.",
            targetFile = "/etc/nginx/conf.d/security_headers.conf",
            language = "NGINX",
            code = """# Dark Avatar Nginx Security Header Stack
server_tokens off;

# TLS Modern Suite (TLS 1.2 & TLS 1.3 only)
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305';
ssl_prefer_server_ciphers on;
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 1d;
ssl_session_tickets off;

# Security Headers
add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
add_header X-Frame-Options "DENY" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "0" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';" always;
add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;
"""
        ),
        HardeningModule(
            title = "UFW / IPTables Perimeter Firewall Script",
            category = "FIREWALL_UFW",
            description = "Default-deny ingress policy with stateful inspection, rate-limiting, and port knocking support.",
            targetFile = "firewall_deploy.sh",
            language = "BASH",
            code = """#!/usr/bin/env bash
# Dark Avatar UFW Deployment Script
set -euo pipefail

echo "[+] Resetting and locking firewall..."
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow Hardened SSH with Rate Limiting
sudo ufw limit 2222/tcp comment 'SSH Rate Limited'

# Allow Web Traffic (HTTP/HTTPS)
sudo ufw allow 80/tcp comment 'HTTP'
sudo ufw allow 443/tcp comment 'HTTPS'

# Enable UFW & Stateful Logging
sudo ufw logging medium
sudo ufw --force enable
echo "[+] UFW Status:"
sudo ufw status verbose
"""
        ),
        HardeningModule(
            title = "Docker Container Isolation & Runtime Lockdown",
            category = "DOCKER_SECURITY",
            description = "Enforces read-only root filesystem, non-root user UID, drops Linux capabilities, and blocks privilege escalation.",
            targetFile = "docker-compose.security.yml",
            language = "YAML",
            code = """version: '3.8'
services:
  app:
    image: my_secure_service:latest
    read_only: true
    user: "10001:10001"
    security_opt:
      - no-new-privileges:true
      - seccomp:unconfined # or path to custom seccomp profile
    cap_drop:
      - ALL
    cap_add:
      - NET_BIND_SERVICE
    tmpfs:
      - /tmp:rw,noexec,nosuid,size=64m
    pids_limit: 100
    mem_limit: 512m
    restart: unless-stopped
"""
        ),
        HardeningModule(
            title = "Android Network Security Config",
            category = "ANDROID_XML",
            description = "Disables cleartext traffic and enforces certificate pinning and system CA trust exclusively.",
            targetFile = "res/xml/network_security_config.xml",
            language = "XML",
            code = """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Strict Disallowance of Cleartext HTTP -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Certificate Pinning for Production Endpoints -->
    <domain-config>
        <domain includeSubdomains="true">api.mydefensehub.com</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
"""
        )
    )

    val currentModule = modules.find { it.category == selectedCategory } ?: modules.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("defense_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            CyberHeader(
                title = "DEFENSE & HARDENING",
                subtitle = "Hardening templates, firewall generators, and OS policy configurations",
                badgeText = "POLICY ENGINE"
            )
        }

        // Category Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionPill(text = "Linux Sysctl", selected = selectedCategory == "LINUX_SYSCTL") {
                    selectedCategory = "LINUX_SYSCTL"
                }
                ActionPill(text = "SSH Daemon", selected = selectedCategory == "SSH_HARDENING") {
                    selectedCategory = "SSH_HARDENING"
                }
                ActionPill(text = "Nginx TLS/CSP", selected = selectedCategory == "NGINX_TLS") {
                    selectedCategory = "NGINX_TLS"
                }
                ActionPill(text = "Firewall UFW", selected = selectedCategory == "FIREWALL_UFW") {
                    selectedCategory = "FIREWALL_UFW"
                }
                ActionPill(text = "Docker Security", selected = selectedCategory == "DOCKER_SECURITY") {
                    selectedCategory = "DOCKER_SECURITY"
                }
                ActionPill(text = "Android Sec Config", selected = selectedCategory == "ANDROID_XML") {
                    selectedCategory = "ANDROID_XML"
                }
            }
        }

        // Active Hardening Template
        item {
            CyberCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                borderColor = CyberCyan
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentModule.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = CyberSurfaceVariant,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, CyberBorderMuted)
                        ) {
                            Text(
                                text = currentModule.targetFile,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentModule.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CodeBlockView(
                        code = currentModule.code,
                        language = currentModule.language
                    )
                }
            }
        }

        // Defensive Checklist
        item {
            Text(
                text = "ACTIONABLE SECURITY AUDIT CHECKLIST",
                style = MaterialTheme.typography.labelMedium,
                color = CyberTextMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChecklistItem(
                    title = "Enforce Strict File Permissions",
                    command = "chmod 600 /etc/shadow && chmod 644 /etc/passwd"
                )
                ChecklistItem(
                    title = "Disable SUID / SGID on Unnecessary Binaries",
                    command = "find / -perm /6000 -type f 2>/dev/null"
                )
                ChecklistItem(
                    title = "Deploy Fail2Ban Intrusion Prevention",
                    command = "sudo apt-get install fail2ban -y && sudo systemctl enable fail2ban"
                )
                ChecklistItem(
                    title = "Verify Inactive User Session Timeouts",
                    command = "echo 'TMOUT=300' >> /etc/profile && readonly TMOUT"
                )
            }
        }
    }
}

@Composable
fun ChecklistItem(title: String, command: String) {
    CyberCard(
        borderColor = CyberBorderMuted
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            com.example.ui.components.CopyableSnippet(
                label = "EXEC",
                text = command
            )
        }
    }
}
