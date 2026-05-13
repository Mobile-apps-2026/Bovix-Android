package pe.edu.upc.bovix.cattle.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AnimalEntity>)

    @Query("SELECT * FROM animals ORDER BY id ASC")
    suspend fun getAll(): List<AnimalEntity>

    @Query("DELETE FROM animals")
    suspend fun clear()
}
