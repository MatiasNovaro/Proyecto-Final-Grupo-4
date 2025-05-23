package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
): MedicationRepository {

    override suspend fun insertMedication(medication: Medication) {
        medicationDao.insertMedication(medication)
    }

    override suspend fun getMedicationsByUser(userId: Int): List<Medication> {
        return medicationDao.getMedicationsByUser(userId)
    }

    override suspend fun getById(id: Int): Medication? {
        return medicationDao.getById(id)
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }
}
