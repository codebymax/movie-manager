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
    fun findAllNew(@PathVariable id: String): MovieJArray {
        var result = MovieJArray(mutableListOf())
        measureTimeMillis({time -> println("Import took $time ms")}) {
            result = MovieJArray(repository.findByUserIdsContains(id.toLong()).map { it.toJson() }.toList())
        }
        return result
    }

    @GetMapping("/id")
    fun findId(@PathVariable id: String,
               @RequestParam movie_id: String): MovieJ {
        var result = emptyMovie()
        measureTimeMillis({time -> println("Import took $time ms")}) {
            val temp = repository.findById(movie_id)
            if (temp.isPresent)
                result = temp.get().toJson()
        }
        return result
    }

    @GetMapping("/count")
    fun countAll(@PathVariable id: String): Int {
        return repository.findAllBy().filter { it.userIds.contains(id.toLong()) }.count()
    }

    @PostMapping("/add/id")
    fun addById(@PathVariable id: String,
                @RequestBody movie_ids: IdListJ): ResponseJ {
        var new = 0
        var reused = 0
        var failed = 0
        var duplicate = 0
        var count = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movie_ids.ids.forEach {
                println(count)
                count++
                val result = repository.findById(it)
                if (result.isEmpty) {
                    val movie = getMovieInfo(null, it, id.toLong())
                    if (movie != null) {
                        repository.save(movie)
                        new++
                    } else {
                        failed++
                    }
                }
                else {
                    if (!result.get().userIds.contains(id.toLong())) {
                        repository.save(result.get().addUser(id.toLong()))
                        reused++
                    }
                    else
                        duplicate++
                }
            }
        }
        println("$new movies added")
        println("$reused movies updated")
        println("$failed movies failed to add")
        println("$duplicate movies already existed")
        return ResponseJ(1, "Added: $new Reused: $reused Failed: $failed Existing: $duplicate")
    }

    @PostMapping("/add")
    fun addMovies(@PathVariable id: String,
                  @RequestBody movies: MovieRequestJ): MovieJArray {
        val array = mutableListOf<MovieJ>()
        var new = 0
        var reused = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movies.movies.forEach {
                val movie = getMovieInfo(it, null, id.toLong())
                val result : Optional<Movie>
                if (movie != null) {
                    result = repository.findByTitleAndYear(movie.title, movie.year)
                    if (result.isPresent) {
                        if (!result.get().userIds.contains(id.toLong())) {
                            repository.save(result.get().addUser(id.toLong()))
                            reused++
                            array.add(result.get().addUser(id.toLong()).toJson())
                        }
                    }
                    else {
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
        repository.deleteAllBy()
        return ResponseJ(1, "N/A")
    }
}