package com.divishare.service

import com.divishare.models.User
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UserCrudService(database: MongoDatabase) {
    private val collection: MongoCollection<Document> = database.getCollection("users")

    // Crear usuario
    suspend fun createUser(user: User): String? = withContext(Dispatchers.IO) {
        val existingUser = collection.find(Filters.or(
            Filters.eq("email", user.email),
            Filters.eq("phoneNumber", user.phoneNumber)
        )).firstOrNull()

        if (existingUser != null) {
            return@withContext null
        }

        // Crear el nuevo usuario si no existe duplicado
        val doc = Document("username", user.username)
            .append("email", user.email)
            .append("passwordHash", user.passwordHash)
            .append("phonePrefix", user.phonePrefix)
            .append("phoneNumber", user.phoneNumber)

        collection.insertOne(doc)
        doc.getObjectId("_id").toString()
    }


    // Leer usuario por ID
    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id)))
            .firstOrNull()
            ?.let { doc ->
                User(
                    id = doc.getObjectId("_id").toString(),
                    username = doc.getString("username"),
                    email = doc.getString("email"),
                    passwordHash = doc.getString("passwordHash"),
                    phonePrefix = doc.getString("phonePrefix"),
                    phoneNumber = doc.getString("phoneNumber")
                )
            }
    }

    suspend fun findUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("email", email))
            .firstOrNull()
            ?.let { doc ->
                User(
                    id = doc.getObjectId("_id").toString(),
                    username = doc.getString("username"),
                    email = doc.getString("email"),
                    passwordHash = doc.getString("passwordHash"),
                    phonePrefix = doc.getString("phonePrefix"),
                    phoneNumber = doc.getString("phoneNumber")
                )
            }
    }

    // Actualizar usuario
    suspend fun updateUser(id: String, user: User): Boolean = withContext(Dispatchers.IO) {
        val updateFields = Document("username", user.username)
            .append("email", user.email)
            .append("passwordHash", user.passwordHash)
            .append("phonePrefix", user.phonePrefix)
            .append("phoneNumber", user.phoneNumber)

        val updateResult = collection.replaceOne(
            Filters.eq("_id", ObjectId(id)),
            updateFields
        )

        return@withContext updateResult.matchedCount > 0
    }

    // Buscar usuario por número de teléfono
    suspend fun findUserByPhone(phonePrefix: String, phoneNumber: String): User? = withContext(Dispatchers.IO) {
        collection.find(Filters.and(
            Filters.eq("phonePrefix", phonePrefix),
            Filters.eq("phoneNumber", phoneNumber)
        )).firstOrNull()
            ?.let { doc ->
                User(
                    id = doc.getObjectId("_id").toString(),
                    username = doc.getString("username"),
                    email = doc.getString("email"),
                    passwordHash = doc.getString("passwordHash"),
                    phonePrefix = doc.getString("phonePrefix"),
                    phoneNumber = doc.getString("phoneNumber")
                )
            }
    }


    // Eliminar usuario
    suspend fun deleteUser(id: String): Boolean = withContext(Dispatchers.IO) {
        val deleteResult = collection.deleteOne(Filters.eq("_id", ObjectId(id)))
        deleteResult.deletedCount > 0
    }
}
