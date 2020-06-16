package com.main.app.controller

import com.main.app.json.SingleMovieRequestJ
import com.main.app.model.Movie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.Normalizer
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

    fun transform(title: String): String {
        val words = listOf("the", "of", "is", "a", "and", "to", "at", "be", "this", "have", "from")
        var result = title
        //remove common words, turn roman numerals to arabic, and remove whitespace
        result = result.split(" ").filter { !words.contains(it.toLowerCase()) }.map { if (checkNumeral(it)) toArabic(it).toString() else it }.joinToString("")
        //turn non-ASCII chars to ASCII
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCOMBINING_DIACRITICAL_MARKS}+")
        result = pattern.matcher(result).replaceAll("")
        //uppercase
        result = result.toUpperCase()
        result = Regex("[^A-Za-z0-9]").replace(result, "")
        return result
    }

    fun checkNumeral(input: String): Boolean {
        val number = input.toUpperCase()
        val valid = listOf<String>("M", "D", "C", "L", "X", "V", "I")
        if(number.split("").filter { valid.contains(it) }.joinToString("").length == number.length)
            return true
        return false
    }

    fun toArabic(input: String): Int {
        val number = input.toUpperCase()

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
        val distance = LevenshteinDistance()
        return (longerLength - distance.apply(longer, shorter)) / longerLength.toDouble()
    }

    fun getMovieInfo(movie: SingleMovieRequestJ, uId: Long): Movie? {
        val client = OkHttpClient.Builder().build()
        val omdbApiKey = "e3801df4"
        val url = "http://www.omdbapi.com/"
        val urlBuilder = HttpUrl.parse(url).newBuilder()
        urlBuilder.addQueryParameter("apikey", omdbApiKey)
        urlBuilder.addQueryParameter("t", movie.title.toLowerCase())
        urlBuilder.addQueryParameter("y", movie.year.toString())
        val request = Request.Builder()
                .url(urlBuilder.build().toString())
                .build()
        val call = client.newCall(request)
        val response = call.execute().body().string()
        val jObject = JSONObject(response)
        var tempYear = 0
        if (jObject.get("Response").toString() == "False") {
            println("Import failed: " + movie.title)
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

        val result = Movie(tempId, mutableListOf(uId), jObject.get("Title").toString(), transform(jObject.get("Title").toString()),
                jObject.get("Plot").toString(), jObject.get("Genre").toString().split(",").map { it.trim() }.toMutableList(),
                jObject.get("Poster").toString(), tempYear, jObject.get("Released").toString(),
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
}