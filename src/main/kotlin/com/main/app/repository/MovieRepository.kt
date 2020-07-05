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

    fun findByUserIdsContains(id: Long): MutableList<Movie>

    fun findBySearchTitleAndYear(title: String, year: Int): Movie
    fun findByTitleAndYear(title: String, year: Int): Optional<Movie>
    fun findByYear(year: Int): MutableList<Movie>
    fun findByYearAndTitleLike(year: Int, title: String): MutableList<Movie>
    fun findByTitleLike(title: String): MutableList<Movie>

    fun findBySearchTitleContains(title: String): MutableList<Movie>
    fun findByYearAndSearchTitleContains(year: Int, title: String): MutableList<Movie>

    fun findByDirector(director: String): MutableList<Movie>

    

    fun deleteById(id: Long)
    fun deleteAllBy()
}