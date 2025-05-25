package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication

interface MedicationRepository {
    suspend fun insertMedication(medication: Medication): Long
    suspend fun getMedicationsByUser(userId: Int): List<Medication>
    suspend fun getById(id: Int): Medication?
    suspend fun deleteMedication(medication: Medication)
}