package ar.ort.edu.proyecto_final_grupo_4.navigation

sealed class Screens(val screen: String) {
    data object Home: Screens("Home")
    data object AddMedication: Screens("add_med")
    data object BiometricLogin : Screens("BiometricLogin")
    data object History: Screens("History")
    data object LoginScreen: Screens("LoginScreen")
    data object RegisterScreen: Screens("RegisterScreen")
    data object Reminder: Screens("Reminder")
    data object EditMedications: Screens("EditMedications")
    data object MedicationDetail : Screens("medication_details/{medicationId}")
}