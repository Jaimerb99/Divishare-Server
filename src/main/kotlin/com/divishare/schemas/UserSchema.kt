package com.divishare.schemas

import com.divishare.models.User
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UserSchemaService(database: MongoDatabase) {
    private val collection: MongoCollection<Document> = database.getCollection("users")

    suspend fun registerUser(user: User): String? = withContext(Dispatchers.IO) {
        val doc = Document("username", user.username)
            .append("email", user.email)
            .append("passwordHash", user.passwordHash)
        collection.insertOne(doc)
        doc.getObjectId("id").toString()
    }

    suspend fun findUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("email", email))
            .firstOrNull()
            ?.let { doc ->
                User(
                    id = doc.getObjectId("id").toString(),
                    username = doc.getString("username"),
                    email = doc.getString("email"),
                    passwordHash = doc.getString("passwordHash")
                )
            }
    }
}
