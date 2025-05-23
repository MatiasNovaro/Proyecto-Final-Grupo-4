package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userID: Int,
    val name: String
)
