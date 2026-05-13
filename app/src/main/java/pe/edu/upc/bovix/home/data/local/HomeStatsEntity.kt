package pe.edu.upc.bovix.home.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_stats")
data class HomeStatsEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val totalAnimals: Int,
    val activeLots: Int,
    val appointmentsToday: Int,
    val alerts: Int,
    val alertTitle: String?,
    val alertDescription: String?
)
