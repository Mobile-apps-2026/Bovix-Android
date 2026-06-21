package pe.edu.upc.bovix.auth.data.remote

import pe.edu.upc.bovix.auth.data.remote.dto.LoginRequestDto
import pe.edu.upc.bovix.auth.data.remote.dto.LoginResponseDto
import pe.edu.upc.bovix.auth.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("user/sign-in")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("user/sign-up")
    suspend fun register(@Body body: RegisterRequestDto): LoginResponseDto
}
