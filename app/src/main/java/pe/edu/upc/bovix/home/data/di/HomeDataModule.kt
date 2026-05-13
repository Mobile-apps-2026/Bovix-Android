package pe.edu.upc.bovix.home.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.bovix.home.data.remote.HomeApi
import pe.edu.upc.bovix.home.data.repository.HomeRepositoryImpl
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}

@Module
@InstallIn(SingletonComponent::class)
object HomeApiModule {
    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)
}
