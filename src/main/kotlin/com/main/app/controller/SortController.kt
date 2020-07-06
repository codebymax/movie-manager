package com.main.app.controller

import com.main.app.json.MovieJArray
import com.main.app.repository.MovieRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/{id}/sort")
class SortController {
    @Autowired
    lateinit var repository: MovieRepository

    @GetMapping("/runtime")
    fun sortRuntime(@PathVariable id: Long,
                    @RequestParam(value = "order", required = true) order: Int): MovieJArray {
        if (order == 0)
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareByDescending { it.runtime }).map { it.toJson() })
        else
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareBy { it.runtime }).map { it.toJson() })
    }

    @GetMapping("/tomato")
    fun sortTomato(@PathVariable id: Long,
                   @RequestParam(value = "order", required = true) order: Int): MovieJArray {
        if (order == 0)
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareByDescending { it.reviews["Rotten Tomatoes"] }).map { it.toJson() })
        else
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareBy { it.reviews["Metascore"] }).map { it.toJson() })
    }

    @GetMapping("/metascore")
    fun sortMetascore(@PathVariable id: Long,
                   @RequestParam(value = "order", required = true) order: Int): MovieJArray {
        if (order == 0)
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareByDescending { it.reviews["Metascore"] }).map { it.toJson() })
        else
            return MovieJArray(repository.findAllBy().filter { it.userIds.contains(id) }.sortedWith(compareBy { it.reviews["Metascore"] }).map { it.toJson() })
    }
}