package ar.ort.edu.proyecto_final_grupo_4.ui.screens

sealed class Screens(val screen: String) {
    data object Home: Screens("home")
    data object AddMedication: Screens("add_med")
    data object BiometricLogin : Screens("BiometricLogin")
    data object History: Screens("History")


}