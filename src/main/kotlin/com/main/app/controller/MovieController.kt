package com.main.app.controller

import com.main.app.json.*
import com.main.app.model.Counter
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlinx.coroutines.*
import kotlin.system.*

@RestController
@RequestMapping("/{id}")
class MovieController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/all")
    fun findAllNew(@PathVariable id: Long): MovieJArray {
        var result = MovieJArray(mutableListOf())
        measureTimeMillis({time -> println("Query took $time ms")}) {
            result = MovieJArray(repository.findByUserIdsContains(id).map { it.toJson() }.toList())
        }
        return result
    }

    @GetMapping("/id")
    fun findId(@PathVariable id: Long,
               @RequestParam movie_id: String): MovieJ {
        var result = emptyMovie()
        measureTimeMillis({time -> println("Search took $time ms")}) {
            val temp = repository.findById(movie_id)
            if (temp.isPresent)
                result = temp.get().toJson()
        }
        return result
    }

    @GetMapping("/count")
    fun countAll(@PathVariable id: Long): Int {
        return repository.findByUserIdsContains(id).count()
    }

    @PostMapping("/add/test")
    fun addTesting(@PathVariable id: Long,
                   @RequestBody movie_ids: IdListJ): ResponseJ {
        val ids = movie_ids.ids.distinct()
        val query = repository.findAllBy()
        val counter = Counter(0)
        runBlocking {
            ids.map { async(Dispatchers.IO) { process(id, it, query, counter) } }
                .map { it.await() }
        }
        println(counter)
        return ResponseJ(1, "N/A")
    }

    fun process(id: Long, input: String, query: MutableList<Movie>, counter: Counter) {
        val result = query.find { movie -> movie.id == input }

        if (result == null) {
            val movie = getMovieInfo(null, input, id, counter)
            if (movie != null)
                repository.save(movie)
        }
        else {
            if (!result.userIds.contains(id))
                repository.save(result.addUser(id))
        }
    }

    @PostMapping("/add/id")
    fun addById(@PathVariable id: Long,
                @RequestBody movie_ids: IdListJ): ResponseJ {
        var new = 0
        var reused = 0
        var failed = 0
        var duplicate = 0
        var count = 0
        val id_list = movie_ids.ids.distinct()
        val query = repository.findAllBy()
        measureTimeMillis({time -> println("Import took $time ms")}) {
            id_list.forEach {
                println(count)
                count++
                val result = query.find { movie -> movie.id == it }

                if (result == null) {
                    val movie = getMovieInfo(null, it, id, Counter(0))
                    if (movie != null) {
                        repository.save(movie)
                        new++
                    } else {
                        failed++
                    }
                }
                else {
                    if (!result.userIds.contains(id)) {
                        repository.save(result.addUser(id))
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
    fun addMovies(@PathVariable id: Long,
                  @RequestBody movies: MovieRequestJ): MovieJArray {
        val array = mutableListOf<MovieJ>()
        var new = 0
        var reused = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movies.movies.forEach {
                val movie = getMovieInfo(it, null, id, Counter(0))
                val result : Optional<Movie>
                if (movie != null) {
                    result = repository.findByTitleAndYear(movie.title, movie.year)
                    if (result.isPresent) {
                        if (!result.get().userIds.contains(id)) {
                            repository.save(result.get().addUser(id))
                            reused++
                            array.add(result.get().addUser(id).toJson())
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
    fun delMovies(@PathVariable id: Long): ResponseJ {
        repository.deleteAllBy()
        return ResponseJ(1, "N/A")
    }
}