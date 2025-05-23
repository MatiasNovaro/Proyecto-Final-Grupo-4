package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit

interface DosageUnitRepository {
    suspend fun insertUnit(unit: DosageUnit)
    suspend fun getAllUnits(): List<DosageUnit>
    suspend fun getById(id: Int): DosageUnit?
}
