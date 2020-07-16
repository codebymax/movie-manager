package com.main.app.controller

import com.main.app.json.GenreJArray
import com.main.app.json.MovieJArray
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import me.xdrop.fuzzywuzzy.FuzzySearch

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
                movies = if (year != null) repository.findByUserIdsContainsAndYear(id, year) else repository.findByUserIdsContains(id)
            }
            movies = movies.filter { FuzzySearch.tokenSortPartialRatio(input, it.title) > 70 }.toMutableList()
            val sortedMovies = movies.sortedWith(compareByDescending { FuzzySearch.tokenSortRatio(input, it.title) }).map { it.toJson() }
            array = MovieJArray(sortedMovies)
        }
        return array
    }

    @GetMapping("/director")
    fun searchByDirector(@PathVariable id: Long,
                         @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findByUserIdsContains(id)
            movies = movies.filter { FuzzySearch.tokenSortPartialRatio(input, it.director.first()) > 70 }.toMutableList()
            array = MovieJArray(movies.sortedWith(compareByDescending { FuzzySearch.tokenSortPartialRatio(input, it.director.first()) }).map { it.toJson() })
        }
        return array
    }

    @GetMapping("/genre/all")
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

    @GetMapping("/genre")
    fun searchByGenre(@PathVariable id: Long,
                      @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            array = MovieJArray(repository.findByUserIdsContainsAndGenresContains(id, input).map { it.toJson() })
        }
        return array
    }

    @GetMapping("/release")
    fun searchByRelease(@PathVariable id: Long,
                      @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            array = MovieJArray(repository.findByUserIdsContainsAndReleaseDate(id, input).map { it.toJson() })
        }
        return array
    }
}