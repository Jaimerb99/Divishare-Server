package com.divishare.schemas

import com.divishare.models.User
import com.divishare.service.UserCrudService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureUserRoutes(userSchemaService: UserCrudService) {
    routing {
        // Crear usuario (registro)
        post("/users/register") {
            val user = call.receive<User>()

            val userId = userSchemaService.createUser(user)

            if (userId != null) {
                call.respond(HttpStatusCode.Created, "User registered with ID: $userId")
            } else {
                call.respond(HttpStatusCode.Conflict, "User with this email or phone number already exists")
            }
        }

        // Leer usuario (protegido)
        authenticate {
            get("/users/id/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val user = userSchemaService.getUserById(id)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            get("/users/email/{email}") {
                val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val user = userSchemaService.findUserByEmail(email)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }



            // Actualizar usuario (protegido)
            put("/users/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val user = call.receive<User>()

                // Validar si el teléfono ya está en uso
                val existingUserByPhone = user.phonePrefix?.let { user.phoneNumber?.let { it1 ->
                    userSchemaService.findUserByPhone(it,
                        it1
                    )
                } }
                if (existingUserByPhone != null && existingUserByPhone.id != id) {
                    return@put call.respond(HttpStatusCode.Conflict, "Phone number already in use")
                }

                val updated = userSchemaService.updateUser(id, user)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "User updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            // Eliminar usuario (protegido)
            delete("/users/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val deleted = userSchemaService.deleteUser(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, "User deleted successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }
        }
    }
}
