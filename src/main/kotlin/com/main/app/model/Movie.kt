package com.main.app.model

import com.main.app.json.MovieJ
import org.springframework.data.annotation.Id
import java.io.Serializable

class Movie (@Id val id: String, val userIds: MutableList<Long>, val title: String,
             private val overview: String, val genres: MutableList<String>,
             private val poster_path: String, val year: Int, val releaseDate: String,
             private val languages: MutableList<String>, val director: MutableList<String>,
             private val cast: MutableList<String>, val runtime: Int,
             var reviews: MutableMap<String, Double>, private val rating: String) : Serializable {

    fun addUser(userId: Long): Movie {
        this.userIds.add(userId)
        return this
    }

    fun toJson(): MovieJ {
        return MovieJ(overview, languages, title, genres, poster_path, year, releaseDate, id, userIds, director, cast, runtime, reviews, rating)
    }

    fun setExtras(map: MutableMap<String, Double>) {
        this.reviews = map
    }
}