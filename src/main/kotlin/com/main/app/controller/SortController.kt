package com.main.app.controller

import com.main.app.json.MovieJArray
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/{id}/sort")
class SortController {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/release")
    fun sortRelease(@PathVariable id: Long,
                    @RequestParam(value = "order", required = true) order: String): MovieJArray {
        if (order == "Desc")
            return MovieJArray(repository.findByUserIdsContainsAndReleaseDateNot(id, "N/A").sortedWith(compareByDescending { LocalDate.parse(it.releaseDate, DateTimeFormatter.ofPattern("d M y")) }).map { it.toJson() })
        else if (order == "Asc")
            return MovieJArray(repository.findByUserIdsContainsAndReleaseDateNot(id, "N/A").sortedWith(compareBy { LocalDate.parse(it.releaseDate, DateTimeFormatter.ofPattern("d M y")) }).map { it.toJson() })
        else
            return MovieJArray(mutableListOf())
    }

    @GetMapping("/runtime")
    fun sortRuntime(@PathVariable id: Long,
                    @RequestParam(value = "order", required = true) order: String): MovieJArray {
        if (order == "Desc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareByDescending { it.runtime }).map { it.toJson() })
        else if (order == "Asc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareBy { it.runtime }).map { it.toJson() })
        else
        return MovieJArray(mutableListOf())
    }

    @GetMapping("/tomato")
    fun sortTomato(@PathVariable id: Long,
                   @RequestParam(value = "order", required = true) order: String): MovieJArray {
        if (order == "Desc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareByDescending { it.reviews["Rotten Tomatoes"] }).map { it.toJson() })
        else if (order == "Asc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareBy { it.reviews["Rotten Tomatoes"] }).map { it.toJson() })
        else
            return MovieJArray(mutableListOf())
    }

    @GetMapping("/metascore")
    fun sortMetascore(@PathVariable id: Long,
                   @RequestParam(value = "order", required = true) order: String): MovieJArray {
        if (order == "Desc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareByDescending { it.reviews["Metascore"] }).map { it.toJson() })
        else if (order == "Asc")
            return MovieJArray(repository.findByUserIdsContains(id).sortedWith(compareBy { it.reviews["Metascore"] }).map { it.toJson() })
        else
            return MovieJArray(mutableListOf())
    }
}