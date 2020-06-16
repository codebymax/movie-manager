package com.main.app.repository

import com.main.app.model.Movie
import com.main.app.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findAllBy(): MutableList<User>
    fun findByUsername(username: String): User
    fun findByUsernameAndPassword(username: String, password: String): User

    fun findById(id: Long): User

    fun findAllByOrderByIdDesc(): MutableList<User>

    fun deleteById(id: Long)
}