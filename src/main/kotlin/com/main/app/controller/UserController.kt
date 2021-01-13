package com.main.app.controller

import com.main.app.json.ResponseJ
import com.main.app.json.UserJ
import com.main.app.model.User
import com.main.app.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController {
    @Autowired
    lateinit var userRepo: UserRepository

    @GetMapping("/auth")
    fun auth(@RequestParam(value = "username", required = true) username: String,
             @RequestParam(value = "password", required = true) password: String): ResponseJ {
        try {
            val user: User = userRepo.findByUsernameAndPassword(username, password)
            return ResponseJ(1, user.id.toString())
        }
        catch (e: EmptyResultDataAccessException) {
            return ResponseJ(0, "Login failed!")
        }
    }

    @PostMapping("/add")
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

    @DeleteMapping("/delete")
    fun delUser(@RequestParam(value = "id", required = true) id: Long): ResponseJ {
        userRepo.deleteById(id)
        return ResponseJ(1, "N/A")
    }

    private fun getNewId(): Long {
        val users = userRepo.findAllByOrderByIdDesc()
        if(users.isNotEmpty())
            return users.first().id+1
        else
            return 0.toLong()
    }
}