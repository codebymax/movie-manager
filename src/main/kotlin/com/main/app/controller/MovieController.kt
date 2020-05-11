package com.main.app.controller

import com.main.app.json.*
import com.main.app.model.Movie
import com.main.app.model.User
import com.main.app.repository.MovieRepository
import com.main.app.repository.UserRepository
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/")
class MovieController {
    @Autowired
    lateinit var repository: MovieRepository

    @Autowired
    lateinit var userRepo: UserRepository

    private var client = OkHttpClient.Builder().build()
    private var movieDB_api_key = "057d0c57465e435ea13e2b857a3d61e2"
    private var omdb_api_key = "e8d12d1"

    @GetMapping("/auth")
    fun auth(@RequestParam(value = "username", required = true) username: String,
             @RequestParam(value = "password", required = true) password: String): ResponseJ {
        try {
            userRepo.findByUsernameAndPassword(username, password)
            return ResponseJ(1, "N/A")
        }
        catch (e: EmptyResultDataAccessException) {
            return ResponseJ(0, "Login failed!")
        }
    }

    @PostMapping("/user/add")
    fun addUser(@RequestBody user: UserJ): ResponseJ {
        try {
            userRepo.findByUsername(user.username)
            return ResponseJ(0, "Username taken")
        }
        catch (e: EmptyResultDataAccessException) {
            userRepo.save(User(getNewId(), user.username, user.password))
            return ResponseJ(1, "N/A")
        }
    }

    @PostMapping("/movie/add")
    fun addMovies(@RequestBody movies: MovieRequestJ): MovieJArray {
        var array = mutableListOf<MovieJ>()
        movies.movies.forEach {
            measureTimeMillis({time -> println("Took $time") }) {
                getMovieInfo(it)?.toJson()
            }?.let { it1 -> array.add(it1) }
        }
        return MovieJArray(array)
    }

    private fun getNewId(): Long {
        val users = userRepo.findAllByOrderByIdDesc()
        if(users.isNotEmpty())
            return users.first().getId()+1
        else
            return 0.toLong()
    }

    fun <T> measureTimeMillis(loggingFunction: (Long) -> Unit,
                                     function: () -> T): T {

        val startTime = System.currentTimeMillis()
        val result: T = function.invoke()
        loggingFunction.invoke(System.currentTimeMillis() - startTime)

        return result
    }

    private fun getMovieInfo(movie: SingleMovieRequestJ): Movie? {
        val url = "https://api.themoviedb.org/3/search/movie"
        val url2 = "http://www.omdbapi.com/"
        var urlBuilder = HttpUrl.parse(url).newBuilder()
        urlBuilder.addQueryParameter("api_key", movieDB_api_key)
        urlBuilder.addQueryParameter("query", movie.title)
        urlBuilder.addQueryParameter("language", "en-US")
        var request = Request.Builder()
                .url(urlBuilder.build().toString())
                .build()
        var call = client.newCall(request)
        var response = call.execute().body().string()

        var Jobject = JSONObject(response)
        var Jarray = Jobject.getJSONArray("results")
        var result: Movie? = null
        for (i in 0 until Jarray.length()) {
            var obj = Jarray.getJSONObject(i)
            if (movie.title == obj.get("title").toString() && movie.year == obj.get("release_date").toString().split("-")[0]) {
                result = Movie(obj.get("title").toString(), /*obj.get("genre_ids").toString().map { it.toInt() }.toMutableList()*/obj.get("release_date").toString(), obj.get("original_language").toString())
                break
            }
        }
        //Now that we have initial info on the movie we are going to make another request for more movie info

        urlBuilder = HttpUrl.parse(url2).newBuilder()
        urlBuilder.addQueryParameter("apikey", omdb_api_key)
        urlBuilder.addQueryParameter("t", movie.title)
        urlBuilder.addQueryParameter("y", movie.year)
        request = Request.Builder()
                .url(urlBuilder.build().toString())
                .build()
        call = client.newCall(request)
        response = call.execute().body().string()
        Jobject = JSONObject(response)
        return result
    }
}