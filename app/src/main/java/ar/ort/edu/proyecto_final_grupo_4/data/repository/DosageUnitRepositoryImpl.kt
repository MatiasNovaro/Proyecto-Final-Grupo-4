package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.DosageUnitDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import javax.inject.Inject

class DosageUnitRepositoryImpl @Inject constructor(
    private val dosageUnitDao: DosageUnitDao
) : DosageUnitRepository {

    override suspend fun insertUnit(unit: DosageUnit) {
        dosageUnitDao.insertUnit(unit)
    }

    override suspend fun getAllUnits(): List<DosageUnit> {
        return dosageUnitDao.getAllUnits()
    }

    override suspend fun getById(id: Int): DosageUnit? {
        return dosageUnitDao.getById(id)
    }
}
