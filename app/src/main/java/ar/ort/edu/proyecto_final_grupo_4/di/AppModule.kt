package ar.ort.edu.proyecto_final_grupo_4.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import ar.ort.edu.proyecto_final_grupo_4.R
import ar.ort.edu.proyecto_final_grupo_4.data.dao.DayOfWeekDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.DosageUnitDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationLogDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduleDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduledAlarmDao
import ar.ort.edu.proyecto_final_grupo_4.data.dao.UserDao
import ar.ort.edu.proyecto_final_grupo_4.data.network.AppMedicamentosDB
import ar.ort.edu.proyecto_final_grupo_4.data.repository.DayOfWeekRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.DosageUnitRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.MedicationLogRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.MedicationRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.ScheduleRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.ScheduledAlarmRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.data.repository.UserRepositoryImpl
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduledAlarmRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.UserRepository
import ar.ort.edu.proyecto_final_grupo_4.services.AlarmCalculatorService
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationAlarmManager
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationSchedulerService
import ar.ort.edu.proyecto_final_grupo_4.services.NotificationDismissalManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideAppMedicamentosDB(
        @ApplicationContext
        context: Context
    ) = Room.databaseBuilder(
        context,
        AppMedicamentosDB::class.java,
        context.resources.getString(R.string.db_name)
    ).fallbackToDestructiveMigration(true).build()

    @Provides
    fun provideMedicationDao(db: AppMedicamentosDB): MedicationDao =
        db.medicationDao()

    @Provides
    fun provideUserDao(db: AppMedicamentosDB): UserDao =
        db.userDao()

    @Provides
    fun provideScheduleDao(db: AppMedicamentosDB): ScheduleDao =
        db.scheduleDao()

    @Provides
    fun provideRepeatDayDao(db: AppMedicamentosDB): DayOfWeekDao =
        db.repeatDayDao()

    @Provides
    fun provideDosageUnitDao(db: AppMedicamentosDB): DosageUnitDao =
        db.dosageUnitDao()

    @Provides
    fun provideMedicationLogDao(db: AppMedicamentosDB): MedicationLogDao =
        db.medicationLogDao()
    @Provides
    fun provideScheduledAlarmDao(db: AppMedicamentosDB): ScheduledAlarmDao {
        return db.scheduleAlarmDao()
    }

    @Provides
    fun provideMedicationRepository(
        medicationDao: MedicationDao): MedicationRepository
    = MedicationRepositoryImpl(medicationDao = medicationDao)

    @Provides
    fun provideScheduleRepository(
        scheduleDao: ScheduleDao
    ): ScheduleRepository = ScheduleRepositoryImpl(scheduleDao = scheduleDao)

    @Provides
    fun provideDayOfWeekRepository(
        dayOfWeekDao: DayOfWeekDao
    ): DayOfWeekRepository = DayOfWeekRepositoryImpl(dayOfWeekDao = dayOfWeekDao)

    @Provides
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository = UserRepositoryImpl(userDao = userDao)

    @Provides
    fun provideMedicationLogRepository(
        medicationLogDao: MedicationLogDao
    ): MedicationLogRepository = MedicationLogRepositoryImpl(medicationLogDao= medicationLogDao)

    @Provides
    fun provideDosageUnitRepository(
        dosageUnitDao: DosageUnitDao
    ): DosageUnitRepository = DosageUnitRepositoryImpl(dosageUnitDao = dosageUnitDao)

    @Provides
    @Singleton
    fun provideNotificationDismissalManager(@ApplicationContext context: Context): NotificationDismissalManager {
        return NotificationDismissalManager(context)
    }
    @Provides // Add this
    @Singleton
    fun provideScheduledAlarmRepository(scheduledAlarmDao: ScheduledAlarmDao): ScheduledAlarmRepository = ScheduledAlarmRepositoryImpl(scheduledAlarmDao= scheduledAlarmDao )

    @Provides
    fun provideMedicationSchedulerService(
        scheduleRepository: ScheduleRepository,
        medicationRepository: MedicationRepository,
        medicationLogRepository: MedicationLogRepository,
        dosageUnitRepository: DosageUnitRepository,
        alarmCalculator: AlarmCalculatorService,
        alarmManager: MedicationAlarmManager,
        notificationDismissalManager: NotificationDismissalManager,
        @ApplicationContext context: Context,
        scheduledAlarmRepository: ScheduledAlarmRepository
    ): MedicationSchedulerService {
        return MedicationSchedulerService(
            scheduleRepository,
            medicationRepository,
            medicationLogRepository,
            dosageUnitRepository,
            alarmCalculator,
            alarmManager,
            context = context,
            notificationDismissalManager = notificationDismissalManager,
            scheduledAlarmRepository = scheduledAlarmRepository,
        )
    }
    @Provides
    fun provideAlarmCalculatorService(dosageUnitRepository: DosageUnitRepository): AlarmCalculatorService = AlarmCalculatorService(dosageUnitRepository)

    @Provides
    fun provideMedicationAlarmManager(
        @ApplicationContext context: Context,
        scheduledAlarmRepository: ScheduledAlarmRepository // Inject the repository here
    ): MedicationAlarmManager {
        return MedicationAlarmManager(context, scheduledAlarmRepository)
    }
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}