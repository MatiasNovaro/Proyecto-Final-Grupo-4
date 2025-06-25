package ar.ort.edu.proyecto_final_grupo_4.data.network

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ar.ort.edu.proyecto_final_grupo_4.data.dao.DayOfWeekDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.DosageUnitDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationLogDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduleDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduledAlarmDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.UserDao
import ar.ort.edu.proyecto_final_grupo_4.data.utils.TimeConverter
import ar.ort.edu.proyecto_final_grupo_4.domain.model.*


@Database(
    version = 13,
    exportSchema = false,
    entities = [
        User::class,
        DosageUnit::class,
        Medication::class,
        Schedule::class,
        DayOfWeek::class,
        MedicationLog::class,
        ScheduledAlarmRecord ::class
    ]
)
@TypeConverters(TimeConverter::class)
abstract class AppMedicamentosDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dosageUnitDao(): DosageUnitDao
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun repeatDayDao(): DayOfWeekDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun scheduleAlarmDao() : ScheduledAlarmDao
}