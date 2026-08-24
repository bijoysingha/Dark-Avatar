package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.FindingEntity
import com.example.data.local.IncidentEntity
import com.example.data.local.ReportEntity
import com.example.data.local.TerminalHistoryEntity
import com.example.data.model.AuditSeverity
import com.example.data.model.CyberMetrics
import com.example.data.model.FindingItem
import com.example.data.model.IncidentItem
import com.example.data.model.ReportItem
import com.example.data.model.TerminalHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CyberRepository(private val database: AppDatabase) {

    val allFindings: Flow<List<FindingItem>> = database.findingDao().getAllFindings()
        .map { list -> list.map { it.toDomainModel() } }

    val allIncidents: Flow<List<IncidentItem>> = database.incidentDao().getAllIncidents()
        .map { list -> list.map { it.toDomainModel() } }

    val allReports: Flow<List<ReportItem>> = database.reportDao().getAllReports()
        .map { list -> list.map { it.toDomainModel() } }

    val terminalHistory: Flow<List<TerminalHistoryItem>> = database.terminalDao().getRecentHistory()
        .map { list -> list.map { it.toDomainModel() } }

    val metrics: Flow<CyberMetrics> = combine(allFindings, allIncidents, allReports) { findings, incidents, reports ->
        val critical = findings.count { it.severity == AuditSeverity.CRITICAL }
        val high = findings.count { it.severity == AuditSeverity.HIGH }
        val total = findings.size
        val score = (100 - (critical * 25) - (high * 10) - (findings.count { it.severity == AuditSeverity.MEDIUM } * 4))
            .coerceIn(10, 100)

        CyberMetrics(
            securityScore = score,
            totalFindings = total,
            criticalFindings = critical,
            highFindings = high,
            activeIncidents = incidents.count { it.stage != com.example.data.model.IncidentStage.VERIFY },
            reportsGenerated = reports.size
        )
    }

    suspend fun insertFinding(finding: FindingItem): Long {
        return database.findingDao().insertFinding(FindingEntity.fromDomainModel(finding))
    }

    suspend fun deleteFinding(id: Long) {
        database.findingDao().deleteFindingById(id)
    }

    suspend fun clearFindings() {
        database.findingDao().clearAllFindings()
    }

    suspend fun insertIncident(incident: IncidentItem): Long {
        return database.incidentDao().insertIncident(IncidentEntity.fromDomainModel(incident))
    }

    suspend fun updateIncident(incident: IncidentItem) {
        database.incidentDao().updateIncident(IncidentEntity.fromDomainModel(incident))
    }

    suspend fun deleteIncident(id: Long) {
        database.incidentDao().deleteIncidentById(id)
    }

    suspend fun clearIncidents() {
        database.incidentDao().clearAllIncidents()
    }

    suspend fun insertReport(report: ReportItem): Long {
        return database.reportDao().insertReport(ReportEntity.fromDomainModel(report))
    }

    suspend fun deleteReport(id: Long) {
        database.reportDao().deleteReportById(id)
    }

    suspend fun clearReports() {
        database.reportDao().clearAllReports()
    }

    suspend fun logTerminalCommand(command: String, output: String, isError: Boolean = false) {
        database.terminalDao().insertCommand(
            TerminalHistoryEntity(
                command = command,
                output = output,
                isError = isError,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearTerminalHistory() {
        database.terminalDao().clearHistory()
    }
}
