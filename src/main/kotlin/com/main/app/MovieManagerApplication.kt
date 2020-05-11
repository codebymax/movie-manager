package com.main.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MovieManagerApplication

fun main(args: Array<String>) {
	runApplication<MovieManagerApplication>(*args)
}
