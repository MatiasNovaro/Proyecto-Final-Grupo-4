package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "dosage_unit")
data class DosageUnit(
    @PrimaryKey(autoGenerate = true)
    val dosageUnitID: Int = 0,
    val name: String
)
