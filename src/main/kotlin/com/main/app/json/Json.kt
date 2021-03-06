package com.main.app.json

import com.fasterxml.jackson.annotation.JsonCreator

data class ResponseJ @JsonCreator constructor(
        val response: Int,
        val message: String
)

data class UserJ @JsonCreator constructor(
        val username: String,
        val password: String
)

data class SingleMovieRequestJ @JsonCreator constructor(
        val title: String,
        val year: Int
)

data class MovieRequestJ @JsonCreator constructor(
        val movies: List<SingleMovieRequestJ>
)

data class IdListJ @JsonCreator constructor(
        val ids: List<String>
)

data class MovieJ @JsonCreator constructor(
        val overview: String,
        val languages: MutableList<String>,
        val title: String,
        val genres: MutableList<String>,
        val posterPath: String,
        val year: Int,
        val release_date: String,
        val id: String,
        val userIds: MutableList<Long>,
        val director: MutableList<String>,
        val cast: MutableList<String>,
        val runtime: Int,
        val reviews: MutableMap<String, Double>,
        val rating: String
)

data class MovieJArray @JsonCreator constructor(
        val movies: List<MovieJ>
)

data class SingleResult @JsonCreator constructor(
        val popularity: Double,
        val vote_count: Int,
        val video: Boolean,
        val posterPath: String,
        val id: Long,
        val adult: Boolean,
        val backdrop_path: String,
        val original_language: String,
        val original_title: String,
        val genre_ids: List<Int>,
        val title: String,
        val vote_average: Double,
        val overview: String,
        val release_date: String
)

data class GenreJArray @JsonCreator constructor(
        val genres: MutableSet<String>
)

data class DBResponse @JsonCreator constructor(
        val page: Int,
        val total_results: Int
)