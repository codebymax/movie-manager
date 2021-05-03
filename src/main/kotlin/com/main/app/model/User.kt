package com.main.app.model

import com.main.app.json.UserJ
import org.springframework.data.annotation.Id

class User (@Id val id: Long, private val username: String, private var password: String) {

}