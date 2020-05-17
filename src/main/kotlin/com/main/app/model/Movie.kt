package com.main.app.model

import com.main.app.json.MovieJ
import org.springframework.data.annotation.Id

class Movie (@Id private val id: String, private val userIds: MutableList<Long>, private val title: String,
             private val searchTitle: String, private val overview: String, private val genres: MutableList<String>,
             private val poster_path: String, private val year: Int, private val release_date: String,
             private val languages: MutableList<String>, private val director: String,
             private val cast: MutableList<String>, private val runtime: Int,
             private var reviews: MutableMap<String, Double>, private val rating: String) {

    fun getId(): String {
        return id
    }

    fun getSearchTitle(): String {
        return searchTitle
    }

    fun getYear(): Int {
        return year
    }

    fun getTitle(): String {
        return title
    }

    fun getUserIds(): MutableList<Long> {
        return userIds
    }

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