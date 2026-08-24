package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FindingDao {
    @Query("SELECT * FROM cyber_findings ORDER BY timestamp DESC")
    fun getAllFindings(): Flow<List<FindingEntity>>

    @Query("SELECT * FROM cyber_findings WHERE id = :id")
    suspend fun getFindingById(id: Long): FindingEntity?

    @Query("SELECT * FROM cyber_findings WHERE severity = :severity ORDER BY timestamp DESC")
    fun getFindingsBySeverity(severity: String): Flow<List<FindingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: FindingEntity): Long

    @Query("DELETE FROM cyber_findings WHERE id = :id")
    suspend fun deleteFindingById(id: Long)

    @Query("DELETE FROM cyber_findings")
    suspend fun clearAllFindings()
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM cyber_incidents ORDER BY updatedAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM cyber_incidents WHERE id = :id")
    suspend fun getIncidentById(id: Long): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("DELETE FROM cyber_incidents WHERE id = :id")
    suspend fun deleteIncidentById(id: Long)

    @Query("DELETE FROM cyber_incidents")
    suspend fun clearAllIncidents()
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM cyber_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM cyber_reports WHERE id = :id")
    suspend fun getReportById(id: Long): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("DELETE FROM cyber_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM cyber_reports")
    suspend fun clearAllReports()
}

@Dao
interface TerminalDao {
    @Query("SELECT * FROM terminal_history ORDER BY timestamp DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<TerminalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(entry: TerminalHistoryEntity): Long

    @Query("DELETE FROM terminal_history")
    suspend fun clearHistory()
}
