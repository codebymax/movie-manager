package com.main.app.model

import com.main.app.json.MovieJ
import org.springframework.data.annotation.Id

class Movie (@Id private val id: Long, private val title: String,
             private val overview: String, private val genres: MutableList<String>,
             private val poster_path: String, private val release_date: String,
             private val language: String, private val director: String,
             private val cast: MutableList<String>, private val runtime: String,
             private val reviews: MutableMap<String, Double>, private val rating: String) {
    constructor(): this(0, "", "", mutableListOf<String>(), "", "", "", "", mutableListOf<String>(), "", mutableMapOf<String, Double>(), "")
    constructor(title: String, release_date: String, language: String): this(0, title, "", mutableListOf<String>(), "", release_date, language, "", mutableListOf<String>(), "", mutableMapOf<String, Double>(), "")
    fun toJson(): MovieJ {
        return MovieJ(overview, language, title, genres, poster_path, release_date, id, director, cast)
    }
}