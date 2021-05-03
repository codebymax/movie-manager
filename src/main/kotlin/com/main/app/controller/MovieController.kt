package com.main.app.controller

import com.main.app.json.*
import com.main.app.model.Counter
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import com.main.app.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.util.Random;

@RestController
@RequestMapping("/{id}")
class MovieController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @Autowired
    lateinit var users: UserRepository

    @GetMapping("/all")
    fun findAllNew(@PathVariable id: Long,
                   @RequestParam(value = "page", required = true) page: Int): MovieJArray {
        if (users.findById(id).isEmpty)
            return MovieJArray(mutableListOf())
        else {
            var result = listOf<MovieJ>()
            measureTimeMillis({ time -> println("Query took $time ms") }) {
                result =repository.findByUserIdsContains(id).map { it.toJson() }.toList()
            }
            if (result.size <= 5)
                return MovieJArray(result)
            else
                return MovieJArray(result.subList((page-1)*5, page*5))
        }
    }

    @GetMapping("/genre")
    fun searchByGenre(@PathVariable id: Long,
                      @RequestParam(value = "genre", required = true) input: String,
                      @RequestParam(value = "page", required = true) page: Int): MovieJArray {
        var result = listOf<MovieJ>()
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            result = repository.findByUserIdsContainsAndGenresContains(id, input).map { it.toJson() }.toList()
        }
        if (result.size <= 5)
            return MovieJArray(result)
        else
            return MovieJArray(result.subList((page-1)*5, page*5))
    }

    @GetMapping("/random")
    fun getRandom(@PathVariable id: Long,
                  @RequestParam(value = "genre", required = false) genre: String?): MovieJArray {
        if (users.findById(id).isEmpty)
            return MovieJArray(mutableListOf())
        else {
            var result = mutableListOf<MovieJ>()
            var temp : List<MovieJ> = listOf()
            measureTimeMillis({ time -> println("Query took $time ms") }) {
                temp = repository.findByUserIdsContains(id).map { it.toJson() }
                if (genre != null) {
                    temp = temp.filter { it.genres.contains(genre) }
                }
            }
            var rand = Random()
            for (i in 0..4) {
                result.add(temp.get(rand.nextInt(temp.size)))
            }
            return MovieJArray(result)
        }
    }

    @GetMapping("/id")
    fun findId(@PathVariable id: Long,
               @RequestParam movie_id: String): MovieJ {
        if (users.findById(id).isEmpty)
            return emptyMovie()
        else {
            var result = emptyMovie()
            measureTimeMillis({ time -> println("Search took $time ms") }) {
                val temp = repository.findById(movie_id)
                if (temp.isPresent)
                    result = temp.get().toJson()
            }
            return result
        }
    }

    @GetMapping("/count")
    fun countAll(@PathVariable id: Long): Int {
        if (users.findById(id).isEmpty)
            return 0
        else
            return repository.findByUserIdsContains(id).size
    }

    @GetMapping("/genre/list")
    fun getAllGenres(@PathVariable id: Long): GenreJArray {
        var genreMap = mutableListOf<MutableList<String>>()
        var array = mutableSetOf<String>()
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findByUserIdsContains(id)
            measureTimeMillis({ time -> println("Processing took $time ms")}) {
                movies.forEach { genreMap.add(it.genres) }
                array = genreMap.flatten().toMutableSet()
            }
        }
        return GenreJArray(array)
    }

    @PostMapping("/add/id")
    fun addTesting(@PathVariable id: Long,
                   @RequestBody movie_ids: IdListJ): ResponseJ {
        if (users.findById(id).isEmpty)
            return ResponseJ(0, "User does not exist")
        else {
            val ids = movie_ids.ids.distinct()
            val query = repository.findAllBy()
            val client = OkHttpClient.Builder().build()
            val counter = Counter()
            runBlocking {
                ids.map { async(Dispatchers.IO) { addId(id, it, query, counter, client) } }
                    .map { it.await() }
            }
            println(counter)
            return ResponseJ(1, "N/A")
        }
    }

    @PostMapping("/add")
    fun addMovies(@PathVariable id: Long,
                  @RequestBody movies: MovieRequestJ): ResponseJ {
        if (users.findById(id).isEmpty)
            return ResponseJ(0, "User does not exist")
        else {
            val query = repository.findAllBy()
            val client = OkHttpClient.Builder().build()
            val counter = Counter()
            measureTimeMillis({ time -> println("Import took $time ms") }) {
                runBlocking {
                    movies.movies.map { async(Dispatchers.IO) { addTitle(id, it, query, counter, client) } }
                            .map { it.await() }
                }
            }
            println(counter)
            return ResponseJ(1, "N/A")
        }
    }

    @DeleteMapping("/delete/all")
    fun delMovies(@PathVariable id: Long): ResponseJ {
        if (users.findById(id).isEmpty)
            return ResponseJ(0, "User does not exist")
        else {
            repository.deleteById(id)
            return ResponseJ(1, "N/A")
        }
    }

    fun addId(id: Long, input: String, query: MutableList<Movie>, counter: Counter, client: OkHttpClient) {
        val result = repository.findById(input)

        if (result.isEmpty) {
            val movie = getMovieInfo(null, input, id, client)
            if (movie != null) {
                repository.save(movie)
                counter.incrementNew()
            } else
                counter.incrementFailed()
        } else {
            if (!result.get().userIds.contains(id)) {
                repository.save(result.get().addUser(id))
                counter.incrementReused()
            } else
                counter.incrementDuplicate()
        }
    }

    fun addTitle(id: Long, input: SingleMovieRequestJ, query: MutableList<Movie>, counter: Counter, client: OkHttpClient) {
        println(input.title)
        val movie = getMovieInfo(input, null, id, client)
        val result: Optional<Movie>
        if (movie != null) {
            result = repository.findById(movie.id)
            if (result.isPresent) {
                if (!result.get().userIds.contains(id)) {
                    repository.save(result.get().addUser(id))
                    counter.incrementReused()
                }
                else
                    counter.incrementDuplicate()
            } else {
                repository.save(movie)
                counter.incrementNew()
            }
        } else {
            counter.incrementFailed()
        }
    }
}