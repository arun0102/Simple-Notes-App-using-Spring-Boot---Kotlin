package arun.pkg.sample_notes_spring_boot.database.repository

import arun.pkg.sample_notes_spring_boot.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
}