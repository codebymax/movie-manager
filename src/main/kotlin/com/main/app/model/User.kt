package com.main.app.model

import org.springframework.data.annotation.Id

class User (@Id private var id: Long, private var username: String, private var password: String) {

    fun getId(): Long {
        return id
    }
}