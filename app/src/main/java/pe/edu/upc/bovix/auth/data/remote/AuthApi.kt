package pe.edu.upc.bovix.auth.data.remote

import pe.edu.upc.bovix.auth.data.remote.dto.LoginRequestDto
import pe.edu.upc.bovix.auth.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto
}
