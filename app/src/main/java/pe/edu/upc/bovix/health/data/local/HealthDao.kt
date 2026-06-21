package pe.edu.upc.bovix.health.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // === Appointment ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppointment(entity: VetAppointmentEntity)

    @Query("SELECT * FROM vet_appointments ORDER BY scheduledAt ASC LIMIT 1")
    suspend fun getNextAppointment(): VetAppointmentEntity?

    @Query("SELECT * FROM vet_appointments ORDER BY scheduledAt ASC")
    fun observeAppointments(): Flow<List<VetAppointmentEntity>>

    @Query("DELETE FROM vet_appointments")
    suspend fun clearAppointments()

    // === Pending vaccinations ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingVaccinations(items: List<PendingVaccinationEntity>)

    @Query("SELECT * FROM pending_vaccinations")
    suspend fun getPendingVaccinations(): List<PendingVaccinationEntity>

    @Query("DELETE FROM pending_vaccinations")
    suspend fun clearPendingVaccinations()

    // === Clinical history ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClinicalEntries(items: List<ClinicalEntryEntity>)

    @Query("SELECT * FROM clinical_entries")
    suspend fun getClinicalEntries(): List<ClinicalEntryEntity>

    @Query("DELETE FROM clinical_entries")
    suspend fun clearClinicalEntries()
}
