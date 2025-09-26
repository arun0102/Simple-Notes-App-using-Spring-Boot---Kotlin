package arun.pkg.sample_notes_spring_boot.controllers

import arun.pkg.sample_notes_spring_boot.database.model.Note
import arun.pkg.sample_notes_spring_boot.database.repository.NoteRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

// POST https://localhost:8080/notes
// GET https://localhost:8080/notes?ownerId=123&other=asdf
// DELETE https://localhost:8080/notes/123

@RestController
@RequestMapping("/notes")
class NoteController(
    private val noteRepository: NoteRepository,
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title cannot be empty")
        val title: String,
        val content: String,
        val color: Long,
        // val ownerId: String,
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant,
    )

    @PostMapping
    fun save(
        @Valid @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val note = noteRepository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                ownerId = ObjectId(ownerId),
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
            )
        )
        return note.toResponse()
    }

    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return noteRepository.findByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteByOwnerId(
        @PathVariable id: String,
    ) {
        val note = noteRepository.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("Note not found")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (note.ownerId.toHexString() == ownerId) {
            noteRepository.deleteById(ObjectId(id))
        }
    }

    private fun Note.toResponse() = NoteResponse(
        id = id.toHexString(),
        title = title,
        content = content,
        color = color,
        createdAt = createdAt
    )
}