package com.main.app.controller

import com.main.app.json.*
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.*
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

@RestController
@RequestMapping("/{id}")
class MovieController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/all")
    fun findAll(@PathVariable id: String): MovieJArray {
        return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id.toLong()) }.map { it.toJson() }.toList())
    }

    @GetMapping("/count")
    fun countAll(@PathVariable id: String): Int {
        return repository.findAllBy().filter { it.userIds.contains(id.toLong()) }.count()
    }

    @PostMapping("/add")
    fun addMovies(@PathVariable id: String,
                  @RequestBody movies: MovieRequestJ): MovieJArray {
        val array = mutableListOf<MovieJ>()
        var new = 0
        var reused = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movies.movies.forEach {
                val movie = getMovieInfo(it, id.toLong())
                val result : Movie
                if (movie != null) {
                    try {
                        result = repository.findByTitleAndYear(movie.title, movie.year)
                        if (!result.userIds.contains(id.toLong())) {
                            repository.save(result.addUser(id.toLong()))
                            reused++
                            array.add(result.addUser(id.toLong()).toJson())
                        }
                    }
                    catch (e: EmptyResultDataAccessException) {
                        repository.save(movie)
                        new++
                        array.add(movie.toJson())
                    }
                } else {
                    println(it.title + ": Failed to add")
                }
            }
        }
        println("$new movies added")
        println("$reused movies updated")
        return MovieJArray(array)
    }

    @DeleteMapping("/delete/all")
    fun delMovies(@PathVariable id: String): ResponseJ {
        repository.deleteById(id.toLong())
        return ResponseJ(1, "N/A")
    }
}