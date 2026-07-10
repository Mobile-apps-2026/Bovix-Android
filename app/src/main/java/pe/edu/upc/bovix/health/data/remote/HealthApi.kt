package pe.edu.upc.bovix.health.data.remote

import pe.edu.upc.bovix.health.data.remote.dto.AppointmentResponseDto
import pe.edu.upc.bovix.health.data.remote.dto.BovineForHealthDto
import pe.edu.upc.bovix.health.data.remote.dto.ClinicalRecordResponseDto
import pe.edu.upc.bovix.health.data.remote.dto.CreateAppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.CreateClinicalRecordDto
import pe.edu.upc.bovix.health.data.remote.dto.UpdateAppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.VaccineResponseDto
import pe.edu.upc.bovix.health.data.remote.dto.VetDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HealthApi {
    // Appointments
    @GET("appointments")
    suspend fun getAppointments(): List<AppointmentResponseDto>

    @POST("appointments")
    suspend fun createAppointment(@Body body: CreateAppointmentDto): AppointmentResponseDto

    @PUT("appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: Int, @Body body: UpdateAppointmentDto): AppointmentResponseDto

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Int)

    // Clinical records
    @GET("clinical-records")
    suspend fun getClinicalRecords(): List<ClinicalRecordResponseDto>

    @POST("clinical-records")
    suspend fun createClinicalRecord(@Body body: CreateClinicalRecordDto): ClinicalRecordResponseDto

    // Vaccines
    @GET("vaccines")
    suspend fun getVaccines(): List<VaccineResponseDto>

    @DELETE("vaccines/{id}")
    suspend fun deleteVaccine(@Path("id") id: Int)

    // Bovines (for bovine selector in clinical records)
    @GET("bovines")
    suspend fun getBovines(): List<BovineForHealthDto>

    // Vets (for vet selector when scheduling)
    @GET("user/vets")
    suspend fun getVets(): List<VetDto>
}
