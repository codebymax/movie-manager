package com.main.app.controller

import com.main.app.json.GenreJArray
import com.main.app.json.MovieJArray
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/{id}/search")
class SearchController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/title")
    fun newSearch(@PathVariable id: Long,
                  @RequestParam(value = "input", required = true) input: String,
                  @RequestParam(value = "year", required = false) year: Int?): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = mutableListOf<Movie>()
            measureTimeMillis({ time -> println("Query took $time ms")}) {
                if (year != null)
                    movies = repository.findByUserIdsContainsAndYearAndSearchTitleContains(id, year, transform(input))
                else
                    movies = repository.findByUserIdsContainsAndSearchTitleContains(id, transform(input))
            }
            val title = transform(input)
            measureTimeMillis({time -> println("Mapping took $time ms")}) {
                val sortedMovies = movies.sortedWith(compareByDescending { similarity(title, it.searchTitle) }).map { it.toJson() }
                array = MovieJArray(sortedMovies)
            }
        }
        return array
    }

    @GetMapping("/director")
    fun searchByDirector(@PathVariable id: Long,
                         @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findAllBy()
            println(movies)
            array = MovieJArray(movies.filter { similarity(transform(input), transform(it.director[0])) > 0.6 }.toMutableList().map { it.toJson() })
        }
        return array
    }

    @GetMapping("/genre/all")
    fun getAllGenres(@PathVariable id: Long): GenreJArray {
        var genreMap = mutableListOf<MutableList<String>>()
        var array = mutableSetOf<String>()
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findAllBy()
            measureTimeMillis({ time -> println("Processing took $time ms")}) {
                filterUser(movies, id).forEach { genreMap.add(it.genres) }
                array = genreMap.flatten().toMutableSet()
            }
        }
        return GenreJArray(array)
    }

    @GetMapping("/genre")
    fun searchByGenre(@PathVariable id: Long,
                      @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findAllBy()
            measureTimeMillis({ time -> println("Processing took $time ms")}) {
                array = MovieJArray(filterUser(movies, id).filter { it.genres.map { genre -> genre.toLowerCase() }.contains(input.toLowerCase()) }.map { it.toJson() })
            }
        }
        return array
    }
}