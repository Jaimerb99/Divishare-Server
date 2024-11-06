package com.divishare

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.divishare.models.User
import com.divishare.schemas.UserSchemaService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureAuthRoutes(userSchemaService: UserSchemaService) {
    routing {
        post("/register") {
            val user = call.receive<User>()
            val userId = userSchemaService.registerUser(user)
            if (userId != null) {
                call.respond(HttpStatusCode.Created, "User registered with ID: $userId")
            } else {
                call.respond(HttpStatusCode.BadRequest, "User registration failed")
            }
        }

        post("/login") {
            val loginUser = call.receive<User>()
            val user = userSchemaService.findUserByEmail(loginUser.email)

            if (user != null && user.passwordHash == loginUser.passwordHash) {
                val token = JWT.create()
                    .withAudience("jwt-audience")
                    .withIssuer("https://jwt-provider-domain/")
                    .withClaim("userId", user.id)
                    .sign(Algorithm.HMAC256("secret"))

                call.respond(HttpStatusCode.OK, mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }
}
