package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override suspend fun insertMedication(medication: Medication) {
        // Validar que el nombre del medicamento no esté vacío
        require(medication.name.isNotBlank()) { "El nombre del medicamento no puede estar vacío." }

        // Validar que la dosis no esté vacía
        require(medication.dosage.isNotBlank()) { "La dosis no puede estar vacía." }

        // Validar que el userID sea válido
        require(medication.userID > 0) { "Debe haber un usuario asignado al medicamento." }

        // Validar que no haya medicamentos duplicados (si es necesario)
        val existingMedication = medicationDao.getMedicationsByUser(medication.userID)
            .any { it.name == medication.name }
        require(!existingMedication) { "El medicamento ya está registrado para este usuario." }

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

