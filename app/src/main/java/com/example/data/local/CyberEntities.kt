package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AuditSeverity
import com.example.data.model.FindingItem
import com.example.data.model.IncidentItem
import com.example.data.model.IncidentStage
import com.example.data.model.ReportItem
import com.example.data.model.TerminalHistoryItem

@Entity(tableName = "cyber_findings")
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val severity: String,
    val location: String,
    val problem: String,
    val whyItMatters: String,
    val secureFix: String,
    val improvedCode: String,
    val rawEvidence: String,
    val timestamp: Long
) {
    fun toDomainModel() = FindingItem(
        id = id,
        title = title,
        category = category,
        severity = try { AuditSeverity.valueOf(severity) } catch (e: Exception) { AuditSeverity.INFO },
        location = location,
        problem = problem,
        whyItMatters = whyItMatters,
        secureFix = secureFix,
        improvedCode = improvedCode,
        rawEvidence = rawEvidence,
        timestamp = timestamp
    )

    companion object {
        fun fromDomainModel(item: FindingItem) = FindingEntity(
            id = item.id,
            title = item.title,
            category = item.category,
            severity = item.severity.name,
            location = item.location,
            problem = item.problem,
            whyItMatters = item.whyItMatters,
            secureFix = item.secureFix,
            improvedCode = item.improvedCode,
            rawEvidence = item.rawEvidence,
            timestamp = item.timestamp
        )
    }
}

@Entity(tableName = "cyber_incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val stage: String,
    val severity: String,
    val affectedSystems: String,
    val iocs: String,
    val containmentActions: String,
    val recoveryPlan: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel() = IncidentItem(
        id = id,
        title = title,
        description = description,
        stage = try { IncidentStage.valueOf(stage) } catch (e: Exception) { IncidentStage.DETECT },
        severity = try { AuditSeverity.valueOf(severity) } catch (e: Exception) { AuditSeverity.HIGH },
        affectedSystems = affectedSystems,
        iocs = iocs,
        containmentActions = containmentActions,
        recoveryPlan = recoveryPlan,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomainModel(item: IncidentItem) = IncidentEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            stage = item.stage.name,
            severity = item.severity.name,
            affectedSystems = item.affectedSystems,
            iocs = item.iocs,
            containmentActions = item.containmentActions,
            recoveryPlan = item.recoveryPlan,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}

@Entity(tableName = "cyber_reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scope: String,
    val executiveSummary: String,
    val findingsCount: Int,
    val criticalCount: Int,
    val highCount: Int,
    val contentMarkdown: String,
    val contentJson: String,
    val createdAt: Long
) {
    fun toDomainModel() = ReportItem(
        id = id,
        title = title,
        scope = scope,
        executiveSummary = executiveSummary,
        findingsCount = findingsCount,
        criticalCount = criticalCount,
        highCount = highCount,
        contentMarkdown = contentMarkdown,
        contentJson = contentJson,
        createdAt = createdAt
    )

    companion object {
        fun fromDomainModel(item: ReportItem) = ReportEntity(
            id = item.id,
            title = item.title,
            scope = item.scope,
            executiveSummary = item.executiveSummary,
            findingsCount = item.findingsCount,
            criticalCount = item.criticalCount,
            highCount = item.highCount,
            contentMarkdown = item.contentMarkdown,
            contentJson = item.contentJson,
            createdAt = item.createdAt
        )
    }
}

@Entity(tableName = "terminal_history")
data class TerminalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val output: String,
    val isError: Boolean,
    val timestamp: Long
) {
    fun toDomainModel() = TerminalHistoryItem(
        id = id,
        command = command,
        output = output,
        isError = isError,
        timestamp = timestamp
    )

    companion object {
        fun fromDomainModel(item: TerminalHistoryItem) = TerminalHistoryEntity(
            id = item.id,
            command = item.command,
            output = item.output,
            isError = item.isError,
            timestamp = item.timestamp
        )
    }
}
