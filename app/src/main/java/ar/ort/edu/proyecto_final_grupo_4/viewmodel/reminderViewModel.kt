package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val dosageUnitRepository: DosageUnitRepository
) : ViewModel() {

    private val _medication = MutableStateFlow<Medication?>(null)
    private val _dosageUnit = MutableStateFlow<DosageUnit?>(null)
    val medication: StateFlow<Medication?> = _medication
    val dosageUnit: StateFlow<DosageUnit?> = _dosageUnit

    fun loadMedication(schedule: Schedule) {
        viewModelScope.launch {
            _medication.value = medicationRepository.getById(schedule.medicationID)
        }
    }

    // --- MODIFIED medicationTaken FUNCTION ---
    fun medicationTaken(schedule: Schedule?) {
        if (schedule != null) {
            viewModelScope.launch {
                val medication = medicationRepository.getById(schedule.medicationID)
                val dosageUnit = medication?.dosageUnitID?.let { dosageUnitRepository.getById(it) }

                if (medication != null) {
                    val medicationLog = MedicationLog(
                        scheduleID = schedule.scheduleID,
                        timestamp = LocalDateTime.now(), // Use java.time.LocalDateTime
                        wasTaken = true,
                        dosageValue = medication.dosage, // Capture current dosage
                        dosageUnit = dosageUnit?.name.orEmpty() // Capture current dosage unit name
                    )
                    medicationLogRepository.insertLog(medicationLog)
                } else {
                    Log.e("ReminderViewModel", "Medication not found for schedule ${schedule.scheduleID} during medicationTaken.")
                }
            }
        }
    }

    // --- MODIFIED medicationNotTaken FUNCTION ---
    fun medicationNotTaken(schedule: Schedule?) {
        if (schedule != null) {
            viewModelScope.launch {
                val medication = medicationRepository.getById(schedule.medicationID)
                val dosageUnit = medication?.dosageUnitID?.let { dosageUnitRepository.getById(it) }

                if (medication != null) {
                    val medicationLog = MedicationLog(
                        scheduleID = schedule.scheduleID,
                        timestamp = LocalDateTime.now(),
                        wasTaken = false,
                        dosageValue = medication.dosage,
                        dosageUnit = dosageUnit?.name.orEmpty()
                    )
                    medicationLogRepository.insertLog(medicationLog)
                } else {
                    // Handle case where medication is not found
                    Log.e("ReminderViewModel", "Medication not found for schedule ${schedule.scheduleID} during medicationNotTaken.")
                }
            }
        }
    }

    fun getDosageUnit (dosageUnitID : Long){
        viewModelScope.launch {
            _dosageUnit.value = dosageUnitRepository.getById(dosageUnitID)
        }
    }
}