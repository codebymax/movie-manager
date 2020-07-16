package com.main.app.model

class Counter (var new: Long, var reused: Long, var failed: Long, var duplicate: Long) {

    constructor() : this(0, 0, 0, 0)

    fun incrementNew() {
        new++
    }

    fun incrementReused() {
        reused++
    }

    fun incrementFailed() {
        failed++
    }

    fun incrementDuplicate() {
        duplicate++
    }

    override fun toString(): String {
        return "Added: $new   Reused: $reused   Failed: $failed   Existing: $duplicate"
    }
}