package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.UserDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.User
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun insertUser(user: User) {
        require(user.name.isNotBlank()) { "El nombre del usuario no puede estar vacío." }
        userDao.insertUser(user)
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    override suspend fun updateUser(user: User) {
        require(user.name.isNotBlank()) { "El nombre del usuario no puede estar vacío." }
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}
