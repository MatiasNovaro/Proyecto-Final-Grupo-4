package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.DosageUnitDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import javax.inject.Inject

class DosageUnitRepositoryImpl @Inject constructor(
    private val dosageUnitDao: DosageUnitDao
) : DosageUnitRepository {

    override suspend fun insertUnit(unit: DosageUnit) {
        // Validar que el nombre de la unidad no esté vacío
        require(unit.name.isNotBlank()) { "El nombre de la unidad de dosificación no puede estar vacío." }

        // Validar que la unidad sea única
        val existingUnit = dosageUnitDao.getAllUnits().any { it.name == unit.name }
        require(!existingUnit) { "Ya existe una unidad con ese nombre." }

        dosageUnitDao.insertUnit(unit)
    }

    override suspend fun getAllUnits(): List<DosageUnit> {
        return dosageUnitDao.getAllUnits()
    }

    override suspend fun getById(id: Int): DosageUnit? {
        return dosageUnitDao.getById(id)
    }
}
