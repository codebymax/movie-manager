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
@RequestMapping("/")
class MovieController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/find/all")
    fun findAll(@RequestParam(value = "userId", required = true) userId: Long): MovieJArray {
        return MovieJArray(repository.findAllBy().filter { it.userIds.contains(userId) }.map { it.toJson() }.toList())
    }

    @PostMapping("/movie/add")
    fun addMovies(@RequestBody movies: MovieRequestJ): MovieJArray {
        val array = mutableListOf<MovieJ>()
        var new = 0
        var reused = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movies.movies.forEach {
                val movie = getMovieInfo(it, movies.userId)
                val result : Movie
                if (movie != null) {
                    try {
                        result = repository.findByTitleAndYear(movie.title, movie.year)
                        if (!result.userIds.contains(movies.userId)) {
                            repository.save(result.addUser(movies.userId))
                            reused++
                            array.add(result.addUser(movies.userId).toJson())
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

    @DeleteMapping("/movie/delete/all")
    fun delMovies(): ResponseJ {
        repository.deleteAllBy()
        return ResponseJ(1, "N/A")
    }
}