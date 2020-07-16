package com.main.app.repository

import com.main.app.model.Movie
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface MovieRepository : MongoRepository<Movie, String> {
    @Cacheable("movieCache")
    fun findAllBy(): MutableList<Movie>

    @Cacheable("movieCache")
    override fun findById(id: String): Optional<Movie>

    @Cacheable("movieCache")
    fun findByUserIdsContains(id: Long): MutableList<Movie>

    @Cacheable("movieCache")
    fun findByTitleAndYear(title: String, year: Int): Optional<Movie>

    @Cacheable("movieCache")
    fun findByUserIdsContainsAndYear(id: Long, year: Int): MutableList<Movie>

    @Cacheable("movieCache")
    fun findByUserIdsContainsAndReleaseDateNot(id: Long, date: String): MutableList<Movie>
    @Cacheable("movieCache")
    fun findByUserIdsContainsAndReleaseDate(id: Long, date: String): MutableList<Movie>

    @Cacheable("movieCache")
    fun findByUserIdsContainsAndGenresContains(id: Long, genre: String): MutableList<Movie>

    fun findByDirector(director: String): MutableList<Movie>

    fun deleteById(id: Long)
}