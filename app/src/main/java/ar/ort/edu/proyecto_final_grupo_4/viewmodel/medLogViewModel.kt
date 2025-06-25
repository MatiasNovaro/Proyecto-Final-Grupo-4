package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.HistoryYUiItem
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map // Import map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicationLogViewModel @Inject constructor(
    private val medicationLogRepository: MedicationLogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _filterByTakenStatus = MutableStateFlow<Boolean?>(null)
    private val _filterByStartDate = MutableStateFlow<LocalDateTime?>(null)
    private val _filterByEndDate = MutableStateFlow<LocalDateTime?>(null)

    // This is the Flow that your UI will observe. It emits HistoryUiItem objects.
    val historyLogs: StateFlow<List<HistoryYUiItem>> =
        combine(
            _filterByTakenStatus,
            _filterByStartDate,
            _filterByEndDate,
            medicationLogRepository.getDetailedMedicationLogs() // This returns Flow<List<HistoryLogDisplayItem>>
        ) { statusFilter, startDateFilter, endDateFilter, rawLogs -> // rawLogs are List<HistoryLogDisplayItem>
            rawLogs
                .filter { log ->
                    val matchesStatus = statusFilter?.let { status ->
                        log.wasTaken == status
                    } ?: true

                    val matchesStartDate = startDateFilter?.let { startDate ->
                        !log.timestamp.toLocalDate().isBefore(startDate.toLocalDate())
                    } ?: true

                    val matchesEndDate = endDateFilter?.let { endDate ->
                        !log.timestamp.toLocalDate().isAfter(endDate.toLocalDate())
                    } ?: true

                    matchesStatus && matchesStartDate && matchesEndDate
                }
                .map { log -> // Map each HistoryLogDisplayItem to a HistoryUiItem
                    // Compute dayLabel here for each log
                    val today = LocalDateTime.now(java.time.ZoneId.systemDefault()).toLocalDate() // Use system default zone
                    val yesterday = today.minusDays(1)
                    val logDate = log.timestamp.toLocalDate()

                    val dayLabel = when (logDate) {
                        today -> "Hoy"
                        yesterday -> "Ayer"
                        else -> {
                            // Example: "Miércoles 18 de Junio"
                            val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
                            log.timestamp.format(formatter)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
                        }
                    }
                    // Create and return the new HistoryUiItem
                    HistoryYUiItem(
                        logID = log.logID,
                        scheduleID = log.scheduleID,
                        medicationName = log.medicationName,
                        dosageValue = log.dosageValue,
                        dosageUnit = log.dosageUnit,
                        wasTaken = log.wasTaken,
                        timestamp = log.timestamp,
                        dayLabel = dayLabel
                    )
                }
                .sortedByDescending { it.timestamp } // Ensure descending order (most recent first)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setFilterByTakenStatus(status: Boolean?) {
        _filterByTakenStatus.value = status
    }

    fun setFilterByDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        _filterByStartDate.value = startDate
        _filterByEndDate.value = endDate
    }
}