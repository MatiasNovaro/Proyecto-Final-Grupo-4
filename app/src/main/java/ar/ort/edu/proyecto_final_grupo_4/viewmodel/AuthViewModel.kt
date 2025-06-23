package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isAuthInitialized: Boolean = false,
    val isUsernameUpdateSuccess: Boolean = false,
    val isPasswordUpdateSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()


    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        _authState.value = _authState.value.copy(
            user = currentUser,
            isAuthInitialized = true,
            isSuccess = if (currentUser != null && !_authState.value.isLoading) true else _authState.value.isSuccess
        )
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null,
                    isSuccess = false
                )

                val result = auth.signInWithEmailAndPassword(email, password).await()

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    user = result.user,
                    isSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getFirebaseErrorMessage(e.errorCode),
                    isSuccess = false,
                    user = null
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("credential") == true -> "Email o contraseña incorrectos"
                    e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                    e.message?.contains("timeout") == true -> "La operación tardó demasiado. Intenta nuevamente"
                    else -> "Email o contraseña incorrectos"
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = errorMessage,
                    isSuccess = false,
                    user = null
                )
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null,
                    isSuccess = false
                )

                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { user ->
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    user = result.user,
                    isSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getFirebaseErrorMessage(e.errorCode),
                    isSuccess = false,
                    user = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Error de conexión. Verifica tu internet",
                    isSuccess = false,
                    user = null
                )
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null,
                    isUsernameUpdateSuccess = false
                )

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    )
                    return@launch
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername.trim())
                    .build()

                currentUser.updateProfile(profileUpdates).await()

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    user = currentUser,
                    isUsernameUpdateSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getFirebaseErrorMessage(e.errorCode),
                    isUsernameUpdateSuccess = false
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                    e.message?.contains("timeout") == true -> "La operación tardó demasiado. Intenta nuevamente"
                    else -> "Error al actualizar el nombre. Intenta nuevamente"
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = errorMessage,
                    isUsernameUpdateSuccess = false
                )
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null,
                    isPasswordUpdateSuccess = false
                )

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    )
                    return@launch
                }

                val email = currentUser.email
                if (email == null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "No se pudo obtener el email del usuario"
                    )
                    return@launch
                }

                // Validar que la nueva contraseña tenga al menos 6 caracteres
                if (newPassword.length < 6) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "La nueva contraseña debe tener al menos 6 caracteres"
                    )
                    return@launch
                }

                // Re-autenticar al usuario con su contraseña actual
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser.reauthenticate(credential).await()

                // Actualizar la contraseña
                currentUser.updatePassword(newPassword).await()

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isPasswordUpdateSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthException) {
                val errorMessage = when (e.errorCode) {
                    "ERROR_WRONG_PASSWORD" -> "La contraseña actual es incorrecta"
                    "ERROR_INVALID_CREDENTIAL" -> "La contraseña actual es incorrecta"
                    "ERROR_WEAK_PASSWORD" -> "La nueva contraseña debe tener al menos 6 caracteres"
                    "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Intenta más tarde"
                    "ERROR_REQUIRES_RECENT_LOGIN" -> "Por seguridad, debes iniciar sesión nuevamente"
                    else -> getFirebaseErrorMessage(e.errorCode)
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = errorMessage,
                    isPasswordUpdateSuccess = false
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                    e.message?.contains("timeout") == true -> "La operación tardó demasiado. Intenta nuevamente"
                    e.message?.contains("credential") == true -> "La contraseña actual es incorrecta"
                    else -> "Error al actualizar la contraseña. Intenta nuevamente"
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = errorMessage,
                    isPasswordUpdateSuccess = false
                )
            }
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            // El AuthStateListener se encargará de actualizar el estado automáticamente
            _authState.value = _authState.value.copy(
                isSuccess = false,
                error = null
            )
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                error = "Error al cerrar sesión"
            )
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearSuccess() {
        _authState.value = _authState.value.copy(isSuccess = false)
    }

    fun clearUsernameUpdateSuccess() {
        _authState.value = _authState.value.copy(isUsernameUpdateSuccess = false)
    }

    fun clearPasswordUpdateSuccess() {
        _authState.value = _authState.value.copy(isPasswordUpdateSuccess = false)
    }

    private fun getFirebaseErrorMessage(errorCode: String): String {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del email es incorrecto"
            "ERROR_BADLY_FORMATTED_REQUEST" -> "Solicitud mal formateada"

            "ERROR_WRONG_PASSWORD" -> "Email o contraseña incorrectos"
            "ERROR_USER_NOT_FOUND" -> "Email o contraseña incorrectos"
            "ERROR_INVALID_CREDENTIAL" -> "Email o contraseña incorrectos"
            "ERROR_INVALID_LOGIN_CREDENTIALS" -> "Email o contraseña incorrectos"

            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Ya existe una cuenta con este email usando otro método"

            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Intenta más tarde"
            "ERROR_OPERATION_NOT_ALLOWED" -> "Operación no permitida. Contacta soporte"

            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este email"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"

            "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Verifica tu internet"
            "ERROR_TIMEOUT" -> "La operación tardó demasiado. Intenta nuevamente"

            else -> when {
                errorCode.contains("INVALID_LOGIN_CREDENTIALS") ||
                        errorCode.contains("INVALID_PASSWORD") ||
                        errorCode.contains("INVALID_EMAIL") -> "Email o contraseña incorrectos"
                errorCode.contains("USER_NOT_FOUND") -> "Email o contraseña incorrectos"
                errorCode.contains("TOO_MANY_REQUESTS") -> "Demasiados intentos. Intenta más tarde"
                else -> "Email o contraseña incorrectos"
            }
        }
    }
}