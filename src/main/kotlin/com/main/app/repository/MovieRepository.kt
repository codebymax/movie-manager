package com.main.app.repository

import com.main.app.model.Movie
import org.springframework.data.mongodb.repository.MongoRepository

interface MovieRepository : MongoRepository<Movie, String> {
    fun findAllBy(): MutableList<Movie>

    fun findById(id: Long): Movie
    fun findByTitle(title: String): MutableList<Movie>
    fun findByDirector(director: String): MutableList<Movie>

    fun deleteById(id: Long)
}