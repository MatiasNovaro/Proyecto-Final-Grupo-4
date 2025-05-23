package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.User

interface UserRepository {
    suspend fun insertUser(user: User)
    suspend fun getUserById(id: Int): User?
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
}
