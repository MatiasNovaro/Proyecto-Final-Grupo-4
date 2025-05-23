package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "dosage_unit")
data class DosageUnit(
    @PrimaryKey val dosageUnitID: Int,
    val name: String
)
