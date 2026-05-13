package pe.edu.upc.bovix.auth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrent(): UserEntity?

    @Query("DELETE FROM users")
    suspend fun clear()
}
