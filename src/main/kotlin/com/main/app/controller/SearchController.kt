package com.main.app.controller

import com.main.app.json.MovieJArray
import com.main.app.model.Movie
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/movie/search")
class SearchController : BaseController() {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/title")
    fun searchByTitle(@RequestParam(value = "userId", required = true) userId: Long,
                     @RequestParam(value = "input", required = true) input: String,
                     @RequestParam(value = "year", required = false) year: Int?): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = mutableListOf<Movie>()
            measureTimeMillis({ time -> println("Query took $time ms")}) {
                movies = repository.findAllBy()
            }
            val title = transform(input)
            measureTimeMillis({time -> println("Mapping took $time ms")}) {
                val movie_map = movies.map { similarity(title, it.searchTitle) to it.toJson() }.toMap().toSortedMap(compareByDescending { it })
                array = MovieJArray(movie_map.values.toMutableList().filter { it.userIds.contains(userId) }.subList(0,4))
            }
        }
        return array
    }

    @GetMapping("/director")
    fun searchByDirector(@RequestParam(value = "userId", required = true) userId: Long,
                       @RequestParam(value = "input", required = true) input: String): MovieJArray {
        var array = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = repository.findAllBy()
            println(movies)
            array = MovieJArray(movies.filter { similarity(transform(input), transform(it.director[0])) > 0.6 }.toMutableList().map { it.toJson() })
        }
        return array
    }
}