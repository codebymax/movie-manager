package com.main.app.controller

import com.main.app.json.*
import com.main.app.model.Movie
import com.main.app.model.User
import com.main.app.repository.MovieRepository
import com.main.app.repository.UserRepository
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.*
import java.text.Normalizer
import java.util.regex.Pattern

@RestController
@RequestMapping("/")
class MovieController {
    @Autowired
    lateinit var repository: MovieRepository

    @Autowired
    lateinit var userRepo: UserRepository

    private var client = OkHttpClient.Builder().build()
    private var movieDB_api_key = "057d0c57465e435ea13e2b857a3d61e2"
    private var omdb_api_key = "e3801df4"

    @GetMapping("/find/all")
    fun findAll(@RequestParam(value = "userId", required = true) userId: Long): MovieJArray {
        return MovieJArray(repository.findAllBy().filter { it.getUserIds().contains(userId) }.map { it.toJson() }.toList())
    }

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

    @GetMapping("/movie/search")
    fun searchMovies(@RequestParam(value = "userId", required = true) userId: Long,
                     @RequestParam(value = "input", required = true) input: String,
                     @RequestParam(value = "year", required = false) year: Int?): MovieJArray {
        var array: MovieJArray = MovieJArray(mutableListOf())
        measureTimeMillis({ time -> println("Search took $time ms")}) {
            var movies = mutableListOf<Movie>()
            measureTimeMillis({ time -> println("Query took $time ms")}) {
                movies = repository.findAllBy()
            }
            val title = transformTitle(input)
            measureTimeMillis({time -> println("Mapping took $time ms")}) {
                var movie_map = movies.map { similarity(title, it.getSearchTitle()) to it.toJson() }.toMap().toSortedMap(compareByDescending { it })
                array = MovieJArray(movie_map.values.toMutableList().filter { it.userIds.contains(userId) }.subList(0,4))
            }
            measureTimeMillis({time -> println("Original method took $time ms")}) {
                movies = movies.filter {
                    val similar = similarity(title, it.getSearchTitle())
                    (year == null && similar > 0.55) || (year != null && similar > 0.45 && year == it.getYear())
                }.toMutableList()
                array = MovieJArray(movies.sortedWith(compareByDescending { similarity(title, it.getSearchTitle()) }).map { it.toJson() })
            }
        }
        return array
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
        var new = 0
        var reused = 0
        measureTimeMillis({time -> println("Import took $time ms")}) {
            movies.movies.forEach {
                var repo = repository.findAllBy()
                val title = transformTitle(it.title)
                val year = it.year
                if (year != null)
                    repo = repo.filter { similarity(title, it.getSearchTitle()) > 0.8 && year == it.getYear() }.toMutableList()
                else
                    repo = repo.filter { similarity(title, it.getSearchTitle()) > 0.8 }.toMutableList()
                if (repo.isEmpty()) {
                    val movie = getMovieInfo(it, movies.userId)
                    if (movie != null) {
                        repository.save(movie)
                        array.add(movie.toJson())
                        new++
                    }
                }
                else {
                    repo = repo.sortedWith(compareByDescending { similarity(title, it.getSearchTitle()) }).toMutableList()
                    if (!repo[0].getUserIds().contains(movies.userId)) {
                        repository.save(repo[0].addUser(movies.userId))
                        reused++
                    }
                    //else
                        //println(it.title + ": was already in the database")
                }
            }
        }
        println("$new movies added")
        println("$reused movies updated")
        return MovieJArray(array)
    }

    @DeleteMapping("/movie/delete/all")
    fun delMovies(): ResponseJ {
        repository.deleteAllBy()
        return ResponseJ(1, "N/A")
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

    private fun getMovieInfo(movie: SingleMovieRequestJ, uId: Long): Movie? {
        val url = "http://www.omdbapi.com/"
        var urlBuilder = HttpUrl.parse(url).newBuilder()
        urlBuilder.addQueryParameter("apikey", omdb_api_key)
        urlBuilder.addQueryParameter("t", movie.title.toLowerCase())
        if (movie.year != null)
            urlBuilder.addQueryParameter("y", movie.year.toString())
        var request = Request.Builder()
                .url(urlBuilder.build().toString())
                .build()
        var call = client.newCall(request)
        var response = call.execute().body().string()
        var jObject = JSONObject(response)
        var tempYear = 0
        if (jObject.get("Response").toString() == "False") {

            if (movie.year != null) {
                println(movie.year.toString() + ": " + movie.title)
                return getMovieInfo(SingleMovieRequestJ(movie.title, null), uId)
            }
            else{
                println("Null year failed: " + movie.title)
                return null
            }
        }
        try {
            if (jObject.has("Year"))
                tempYear = jObject.get("Year").toString().toInt()
        }
        catch (e: java.lang.NumberFormatException) {

        }
        var tempRun = 0
        try {
            if (jObject.has("Runtime"))
                tempRun = jObject.get("Runtime").toString().split(" ")[0].toInt()
        }
        catch (e: java.lang.NumberFormatException) {

        }
        var tempId = "N/A"
        if (jObject.has("imdbID"))
            tempId = jObject.get("imdbID").toString()

        val result = Movie(tempId, mutableListOf(uId), jObject.get("Title").toString(), transformTitle(jObject.get("Title").toString()),
                jObject.get("Plot").toString(), jObject.get("Genre").toString().split(",").map { it.trim() }.toMutableList(),
                jObject.get("Poster").toString(), tempYear, jObject.get("Released").toString(),
                jObject.get("Language").toString().split(",").map { it.trim() }.toMutableList(), jObject.get("Director").toString(),
                jObject.get("Actors").toString().split(",").map { it.trim() }.toMutableList(), tempRun,
                mutableMapOf(), jObject.get("Rated").toString())
        val map = mutableMapOf<String, Double>()
        val jArray = jObject.getJSONArray("Ratings")
        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
            if (obj.get("Source").toString() == "Rotten Tomatoes") {
                map["Rotten Tomatoes"] = obj.get("Value").toString().split("%")[0].toDouble()
                break
            }
        }
        if (jObject.has("Metascore") && jObject.get("Metascore").toString() != "N/A")
            map["Metascore"] = jObject.get("Metascore").toString().toDouble()
        if (jObject.has("imdb") && jObject.get("imdb").toString() != "N/A")
            map["imdb"] = jObject.get("imdbRating").toString().toDouble()

        result.setExtras(map)

        return result
    }

    fun transformTitle(title: String): String {
        var words = listOf("the", "of", "is", "a", "and", "to", "at", "be", "this", "have", "from")
        var result = title
        //remove common words, turn roman numerals to arabic, and remove whitespace
        result = result.split(" ").filter { !words.contains(it.toLowerCase()) }.map { if (checkNumeral(it)) toArabic(it).toString() else it }.joinToString("")
        //turn non-ASCII chars to ASCII
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
        var pattern = Pattern.compile("\\p{InCOMBINING_DIACRITICAL_MARKS}+")
        result = pattern.matcher(result).replaceAll("")
        //uppercase
        result = result.toUpperCase()
        result = Regex("[^A-Za-z0-9]").replace(result, "")
        return result
    }

    fun checkNumeral(input: String): Boolean {
        var number = input.toUpperCase()
        var valid = listOf<String>("M", "D", "C", "L", "X", "V", "I")
        if(number.split("").filter { valid.contains(it) }.joinToString("").length == number.length)
            return true
        return false
    }

    fun toArabic(input: String): Int {
        var number = input.toUpperCase()

        if (number.startsWith("M")) return 1000 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("CM")) return 900 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("D")) return 500 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("CD")) return 400 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("C")) return 100 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("XC")) return 90 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("L")) return 50 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("XL")) return 40 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("X")) return 10 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("IX")) return 9 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("V")) return 5 + toArabic(number.removeRange(0, 1))
        if (number.startsWith("IV")) return 4 + toArabic(number.removeRange(0, 2))
        if (number.startsWith("I")) return 1 + toArabic(number.removeRange(0, 1))
        return 0
    }

    fun similarity( s1: String, s2: String): Double {
        var longer = s1
        var shorter = s2
        if (s1.length < s2.length) {
            longer = s2
            shorter = s1
        }
        val longerLength = longer.length
        if (longerLength == 0)
            return 1.0
        var distance = LevenshteinDistance()
        return (longerLength - distance.apply(longer, shorter)) / longerLength.toDouble()
    }
}