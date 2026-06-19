package pe.edu.upc.bovix.feed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeding_plans")
data class FeedingPlanEntity(
    @PrimaryKey val id: String,
    val lot: String,
    val dailyRationKg: Double,
    val animalCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "feeding_components")
data class FeedingComponentEntity(
    @PrimaryKey val id: String,
    val planId: String,        // FK lógica
    val name: String,
    val percentage: Int,
    val amountKg: Double,
    val color: String
)
