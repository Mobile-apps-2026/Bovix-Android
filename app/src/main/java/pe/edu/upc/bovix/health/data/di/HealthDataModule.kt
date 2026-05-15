package pe.edu.upc.bovix.health.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.bovix.health.data.remote.HealthApi
import pe.edu.upc.bovix.health.data.repository.HealthRepositoryImpl
import pe.edu.upc.bovix.health.domain.repository.HealthRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object HealthApiModule {
    @Provides
    @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
