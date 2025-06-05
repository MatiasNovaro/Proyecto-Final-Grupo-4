package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository
) : ViewModel() {

    private val _medication = MutableStateFlow<Medication?>(null)
    val medication: StateFlow<Medication?> = _medication

    fun loadMedication(schedule: Schedule) {
        viewModelScope.launch {
            _medication.value = medicationRepository.getById(schedule.medicationID)
        }
    }

    fun medicationTaken(schedule: Schedule?) {
        if (schedule != null) {
            viewModelScope.launch {
                val medicationLog= MedicationLog(
                    scheduleID = schedule.scheduleID,
                    timestamp = java.time.LocalDateTime.now(),
                    wasTaken = true
                )
                medicationLogRepository.insertLog(medicationLog)
            }
        }
    }

    fun medicationNotTaken(schedule: Schedule?) {
        if (schedule != null) {
            viewModelScope.launch {
                // Lógica para registrar que el medicamento no fue tomado
                val medicationLog= MedicationLog(
                    scheduleID = schedule.scheduleID,
                    timestamp = java.time.LocalDateTime.now(),
                    wasTaken = false
                )
                medicationLogRepository.insertLog(medicationLog)
            }
        }
    }
}