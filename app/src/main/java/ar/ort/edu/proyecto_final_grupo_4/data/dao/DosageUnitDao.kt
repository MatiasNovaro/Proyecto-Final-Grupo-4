package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit

@Dao
interface DosageUnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: DosageUnit)

    @Query("SELECT * FROM dosage_unit")
    suspend fun getAllUnits(): List<DosageUnit>

    @Query("SELECT * FROM dosage_unit WHERE dosageUnitID = :id")
    suspend fun getById(id: Int): DosageUnit?
}