package com.main.app.controller

import com.main.app.json.MovieJ
import com.main.app.json.SingleMovieRequestJ
import com.main.app.model.Counter
import com.main.app.model.Movie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Error
import java.net.SocketTimeoutException
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/*
This file contains functions to be used by the other controller files.
By putting these functions in a parent class it centralizes functions
that are needed by multiple controllers and reduces code repetition.
 */

@RestController
@RequestMapping("/")
class BaseController {
    fun <T> measureTimeMillis(loggingFunction: (Long) -> Unit,
                              function: () -> T): T {

        val startTime = System.currentTimeMillis()
        val result: T = function.invoke()
        loggingFunction.invoke(System.currentTimeMillis() - startTime)

        return result
    }

    fun getMovieInfo(movie: SingleMovieRequestJ?, movie_id: String?, uId: Long, client: OkHttpClient): Movie? {
        val omdbApiKey = "e3801df4"
        val url = "http://www.omdbapi.com/"
        val urlBuilder = HttpUrl.parse(url).newBuilder()
        urlBuilder.addQueryParameter("apikey", omdbApiKey)
        if (movie != null) {
            urlBuilder.addQueryParameter("t", movie.title.toLowerCase())
            urlBuilder.addQueryParameter("y", movie.year.toString())
        }
        else {
            urlBuilder.addQueryParameter("i", movie_id)
        }
        val request = Request.Builder()
                .url(urlBuilder.build().toString())
                .build()
        val call = client.newCall(request)
        var response = ""
        try {
            response = call.execute().body().string()
        } catch (e: SocketTimeoutException) {
            println(e.message)
            println("Socket Timeout")
            return null
        }
        val jObject = JSONObject(response)
        var tempYear = 0
        if (jObject.get("Response").toString() == "False") {
            if (movie != null)
                println("Import failed: " + movie.title)
            else
                println("Import failed: $movie_id")
            return null
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

        val result = Movie(tempId, mutableListOf(uId), jObject.get("Title").toString(),
                jObject.get("Plot").toString(), jObject.get("Genre").toString().split(",").map { it.trim() }.toMutableList(),
                jObject.get("Poster").toString(), tempYear, convertDate(jObject.get("Released").toString()),
                jObject.get("Language").toString().split(",").map { it.trim() }.toMutableList(), jObject.get("Director").toString().split(",").map { it.trim() }.toMutableList(),
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

    fun convertDate(date: String): String {
        try {
            val arr = date.split(" ")
            val map: HashMap<String, Int> = hashMapOf("Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4, "May" to 5, "Jun" to 6, "Jul" to 7, "Aug" to 8, "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12)
            return arr[0] + " " + map[arr[1]].toString() + " " + arr[2]
        } catch (e: Exception) {
            return "N/A"
        }
    }

    fun emptyMovie(): MovieJ {
        return MovieJ("", mutableListOf(), "", mutableListOf(), "", 0, "", "", mutableListOf(), mutableListOf(), mutableListOf(), 0, mutableMapOf(), "")
    }
}