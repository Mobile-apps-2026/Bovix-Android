package pe.edu.upc.bovix.cattle.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.bovix.cattle.data.remote.CattleApi
import pe.edu.upc.bovix.cattle.data.repository.CattleRepositoryImpl
import pe.edu.upc.bovix.cattle.domain.repository.CattleRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CattleRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCattleRepository(impl: CattleRepositoryImpl): CattleRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CattleApiModule {
    @Provides
    @Singleton
    fun provideCattleApi(retrofit: Retrofit): CattleApi = retrofit.create(CattleApi::class.java)
}
