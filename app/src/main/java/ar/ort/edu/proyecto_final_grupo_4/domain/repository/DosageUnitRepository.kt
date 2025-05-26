package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit

interface DosageUnitRepository {
    suspend fun insertUnit(unit: DosageUnit) :Long
    suspend fun getAllUnits(): List<DosageUnit>
    suspend fun getById(id: Long): DosageUnit?
}
