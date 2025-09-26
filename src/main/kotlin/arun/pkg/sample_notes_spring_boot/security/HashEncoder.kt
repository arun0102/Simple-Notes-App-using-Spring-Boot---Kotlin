package arun.pkg.sample_notes_spring_boot.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {

    private val bCrypt = BCryptPasswordEncoder()

    fun encode(password: String) = bCrypt.encode(password)

    fun matches(password: String, encodedPassword: String) = bCrypt.matches(password, encodedPassword)
}