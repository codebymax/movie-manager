package com.main.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class MovieManagerApplication

fun main(args: Array<String>) {
	runApplication<MovieManagerApplication>(*args)
}
