package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Query("SELECT * FROM medication WHERE userID = :userId")
    suspend fun getMedicationsByUser(userId: Int): List<Medication>

    @Query("SELECT * FROM medication WHERE medicationID = :id")
    suspend fun getById(id: Long): Medication?

    @Delete
    suspend fun deleteMedication(medication: Medication)
}