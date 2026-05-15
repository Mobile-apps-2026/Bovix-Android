package pe.edu.upc.bovix.health.data.remote

import pe.edu.upc.bovix.health.data.remote.dto.AppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.ClinicalRecordDto
import pe.edu.upc.bovix.health.data.remote.dto.PendingVaccineDto
import retrofit2.http.GET

interface HealthApi {
    @GET("appointments/next")
    suspend fun getNextAppointment(): AppointmentDto?

    @GET("vaccines/pending")
    suspend fun getPendingVaccines(): List<PendingVaccineDto>

    @GET("clinical-records")
    suspend fun getClinicalRecords(): List<ClinicalRecordDto>
}
