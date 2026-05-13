package pe.edu.upc.bovix.cattle.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lot: String,
    val status: String,
    val gender: String,
    val weightKg: Int
)
