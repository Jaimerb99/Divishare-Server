package com.divishare.plugins

import com.divishare.schemas.configureUserRoutes
import com.divishare.service.UserCrudService

import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoClients
import io.ktor.server.application.*
import io.ktor.server.config.*

fun Application.configureDatabases() {
    val database = connectToMongoDB()
    val userSchemaService = UserCrudService(database)
    configureUserRoutes(userSchemaService)
}

/**
 * Conecta a una base de datos MongoDB utilizando la configuración de la aplicación.
 *
 * @returns [MongoDatabase] instancia de base de datos
 */
fun Application.connectToMongoDB(): MongoDatabase {
    val user = environment.config.tryGetString("db.mongo.user")
    val password = environment.config.tryGetString("db.mongo.password")
    val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
    val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

    val credentials = user?.let { u -> password?.let { p -> "$u:$p@" } }.orEmpty()
    val uri = "mongodb://$credentials$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
