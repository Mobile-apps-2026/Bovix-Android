package pe.edu.upc.bovix.feed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedingDao {
    // === Plans ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlans(items: List<FeedingPlanEntity>)

    @Query("SELECT * FROM feeding_plans ORDER BY lot ASC")
    suspend fun getPlans(): List<FeedingPlanEntity>

    @Query("DELETE FROM feeding_plans")
    suspend fun clearPlans()

    // === Components ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComponents(items: List<FeedingComponentEntity>)

    @Query("SELECT * FROM feeding_components WHERE planId = :planId")
    suspend fun getComponentsForPlan(planId: String): List<FeedingComponentEntity>

    @Query("DELETE FROM feeding_components")
    suspend fun clearComponents()
}
