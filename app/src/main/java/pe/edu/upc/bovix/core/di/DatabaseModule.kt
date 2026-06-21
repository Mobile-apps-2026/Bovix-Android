package pe.edu.upc.bovix.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.bovix.core.database.BovixDatabase
import pe.edu.upc.bovix.auth.data.local.UserDao
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BovixDatabase =
        Room.databaseBuilder(
            context,
            BovixDatabase::class.java,
            "bovix.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideUserDao(db: BovixDatabase): UserDao = db.userDao()

    @Provides
    fun provideHomeStatsDao(db: BovixDatabase): HomeStatsDao = db.homeStatsDao()

    @Provides
    fun provideAnimalDao(db: BovixDatabase): AnimalDao = db.animalDao()

    @Provides
    fun provideHealthDao(db: BovixDatabase): HealthDao = db.healthDao()

    @Provides
    fun provideFeedingDao(db: BovixDatabase): FeedingDao = db.feedingDao()
}
