package pe.edu.upc.bovix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.edu.upc.bovix.auth.data.local.UserDao
import pe.edu.upc.bovix.auth.data.local.UserEntity
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.cattle.data.local.AnimalEntity
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import pe.edu.upc.bovix.home.data.local.HomeStatsEntity

@Database(
    entities = [
        UserEntity::class,
        HomeStatsEntity::class,
        AnimalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BovixDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun homeStatsDao(): HomeStatsDao
    abstract fun animalDao(): AnimalDao
}
