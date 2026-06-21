package pe.edu.upc.bovix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.edu.upc.bovix.auth.data.local.UserDao
import pe.edu.upc.bovix.auth.data.local.UserEntity
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.cattle.data.local.AnimalEntity
import pe.edu.upc.bovix.health.data.local.ClinicalEntryEntity
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.health.data.local.PendingVaccinationEntity
import pe.edu.upc.bovix.health.data.local.VetAppointmentEntity
import pe.edu.upc.bovix.feed.data.local.FeedingComponentEntity
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.feed.data.local.FeedingPlanEntity
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import pe.edu.upc.bovix.home.data.local.HomeStatsEntity

@Database(
    entities = [
        UserEntity::class,
        HomeStatsEntity::class,
        AnimalEntity::class,
        VetAppointmentEntity::class,
        PendingVaccinationEntity::class,
        ClinicalEntryEntity::class,
        FeedingPlanEntity::class,
        FeedingComponentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BovixDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun homeStatsDao(): HomeStatsDao
    abstract fun animalDao(): AnimalDao
    abstract fun healthDao(): HealthDao
    abstract fun feedingDao(): FeedingDao
}
