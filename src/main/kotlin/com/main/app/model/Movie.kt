package com.main.app.model

import com.main.app.json.MovieJ
import org.springframework.data.annotation.Id
import java.io.Serializable

class Movie (@Id val id: String, val userIds: MutableList<Long>, val title: String,
             val searchTitle: String, private val overview: String, private val genres: MutableList<String>,
             private val poster_path: String, val year: Int, private val release_date: String,
             private val languages: MutableList<String>, val director: MutableList<String>,
             private val cast: MutableList<String>, private val runtime: Int,
             private var reviews: MutableMap<String, Double>, private val rating: String) : Serializable {

    fun addUser(userId: Long): Movie {
        this.userIds.add(userId)
        return this
    }

    fun toJson(): MovieJ {
        return MovieJ(overview, languages, searchTitle, title, genres, poster_path, year, release_date, id, userIds, director, cast, runtime, reviews, rating)
    }

    fun setExtras(map: MutableMap<String, Double>) {
        this.reviews = map
    }
}