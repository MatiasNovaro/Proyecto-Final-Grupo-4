package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.User
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun ensureDefaultUser(name: String = "Usuario") {
        viewModelScope.launch {
            val existing = userRepository.getUserById(1)
            if (existing == null) {
                val user = User(userID = 1, name = name)
                userRepository.insertUser(user)
                _user.value = user
            } else {
                _user.value = existing
            }
        }
    }
}
