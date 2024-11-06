package com.divishare

import com.divishare.plugins.*
import com.divishare.schemas.UserSchemaService
import com.divishare.schemas.configureUserRoutes
import com.divishare.service.UserCrudService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()  // Configuración de autenticación (JWT)
    configureSerialization()  // Serialización
    configureDatabases()  // Configuración de la base de datos

    // Conectar la base de datos y configurar el servicio de usuario
    val database = connectToMongoDB()
    val userSchemaService = UserSchemaService(database)
    val userCrudService = UserCrudService(database)

    // Configurar rutas de autenticación y CRUD de usuario
    configureAuthRoutes(userSchemaService)
    configureUserRoutes(userCrudService)
}
