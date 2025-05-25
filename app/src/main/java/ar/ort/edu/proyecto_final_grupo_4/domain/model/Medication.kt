package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DosageUnit::class,
            parentColumns = ["dosageUnitID"],
            childColumns = ["dosageUnitID"]
        )
    ]

)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medicationID: Long = 0,
    val name: String,
    val userID: Int,
    val dosageUnitID: Int,
    val dosage: String
)
