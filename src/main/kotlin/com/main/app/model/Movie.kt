package com.main.app.model

import com.main.app.json.MovieJ
import org.springframework.data.annotation.Id

class Movie (@Id private val id: String, private val title: String,
             private val overview: String, private val genres: MutableList<String>,
             private val poster_path: String, private val year: Int, private val release_date: String,
             private val languages: MutableList<String>, private val director: String,
             private val cast: MutableList<String>, private val runtime: Int,
             private var reviews: MutableMap<String, Double>, private val rating: String) {
    constructor(): this("", "", "", mutableListOf<String>(), "", 0, "", mutableListOf(), "", mutableListOf<String>(), 0, mutableMapOf<String, Double>(), "")

    fun toJson(): MovieJ {
        return MovieJ(overview, languages, title, genres, poster_path, year, release_date, id, director, cast, runtime, reviews, rating)
    }

    fun setExtras(map: MutableMap<String, Double>) {
        this.reviews = map
    }
}