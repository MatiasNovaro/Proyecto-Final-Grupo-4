package ar.ort.edu.proyecto_final_grupo_4.services

import android.annotation.SuppressLint
import android.util.Log
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationAlarm
import java.time.LocalDateTime

class AlarmTester(private val alarmManager: MedicationAlarmManager) {

    @SuppressLint("ScheduleExactAlarm")
    fun testAlarmTypes() {
        Log.d("AlarmTester", "🧪 Starting alarm type tests...")

        // Test 1: Alarma inmediata (debería funcionar)
        val immediateAlarm = MedicationAlarm(
            scheduleId = 999L,
            medicationName = "Test Immediate",
            dosage = "1 pastilla",
            dosageUnit = "pastilla",
            scheduledTime = LocalDateTime.now().plusSeconds(10)
        )

        Log.d("AlarmTester", "Test 1: Immediate alarm")
        alarmManager.scheduleAlarm(immediateAlarm)

        // Test 2: Alarma en 2 minutos
        val futureAlarm = MedicationAlarm(
            scheduleId = 998L,
            medicationName = "Test Future",
            dosage = "2 pastillas",
            dosageUnit = "pastilla",
            scheduledTime = LocalDateTime.now().plusMinutes(2)
        )

        Log.d("AlarmTester", "Test 2: Future alarm (2 minutes)")
        alarmManager.scheduleAlarm(futureAlarm)

        // Test 3: Verificar diferencias de tipo
        val calculatedTime = LocalDateTime.of(2025, 6, 13, 15, 30, 0)
        val calculatedAlarm = MedicationAlarm(
            scheduleId = 997L,
            medicationName = "Test Calculated",
            dosage = "1 cápsula",
            dosageUnit = "cápsula",
            scheduledTime = calculatedTime
        )

        Log.d("AlarmTester", "Test 3: Calculated time alarm")
        Log.d("AlarmTester", "Calculated time type: ${calculatedTime::class.java}")
        alarmManager.scheduleAlarm(calculatedAlarm)
    }
}