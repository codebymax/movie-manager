package com.main.app.model

class Counter (var count: Long) {

    fun increment() {
        count++
    }

    override fun toString(): String {
        return "Counted: $count"
    }
}