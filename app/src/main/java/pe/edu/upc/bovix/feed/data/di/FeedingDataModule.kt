package pe.edu.upc.bovix.feed.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.bovix.feed.data.remote.FeedingApi
import pe.edu.upc.bovix.feed.data.repository.FeedingRepositoryImpl
import pe.edu.upc.bovix.feed.domain.repository.FeedingRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedingRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFeedingRepository(impl: FeedingRepositoryImpl): FeedingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FeedingApiModule {
    @Provides
    @Singleton
    fun provideFeedingApi(retrofit: Retrofit): FeedingApi = retrofit.create(FeedingApi::class.java)
}
