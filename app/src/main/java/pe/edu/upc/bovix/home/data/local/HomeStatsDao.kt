package pe.edu.upc.bovix.home.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HomeStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HomeStatsEntity)

    @Query("SELECT * FROM home_stats WHERE id = 1")
    suspend fun get(): HomeStatsEntity?

    @Query("DELETE FROM home_stats")
    suspend fun clear()
}
