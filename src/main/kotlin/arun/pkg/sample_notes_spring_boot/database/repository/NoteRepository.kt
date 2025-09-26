package arun.pkg.sample_notes_spring_boot.database.repository

import arun.pkg.sample_notes_spring_boot.database.model.Note
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository: MongoRepository<Note, ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<Note>
}