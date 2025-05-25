package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val dosageUnitRepository: DosageUnitRepository
) : ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    private val _dosageUnits = MutableStateFlow<List<DosageUnit>>(emptyList())
    val dosageUnits: StateFlow<List<DosageUnit>> = _dosageUnits

    fun loadMedications(userId: Int) {
        viewModelScope.launch {
            _medications.value = medicationRepository.getMedicationsByUser(userId)
        }
    }

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.insertMedication(medication)
            loadMedications(medication.userID)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)
            loadMedications(medication.userID)
        }
    }

    fun loadDosageUnits() {
        viewModelScope.launch {
            _dosageUnits.value = dosageUnitRepository.getAllUnits()
        }
    }

    fun addDosageUnit(unit: DosageUnit) {
        viewModelScope.launch {
            dosageUnitRepository.insertUnit(unit)
            loadDosageUnits()
        }
    }
}
