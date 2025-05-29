package ar.ort.edu.proyecto_final_grupo_4.ui.screens

sealed class Screens(val screen: String) {
    data object BiometricLogin : Screens("BiometricLogin")
    data object Home: Screens("Home")
    data object AddMedication: Screens("AddMedication")
    data object History: Screens("History")


}