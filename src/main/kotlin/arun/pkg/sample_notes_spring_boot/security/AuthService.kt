package arun.pkg.sample_notes_spring_boot.security

import arun.pkg.sample_notes_spring_boot.database.model.RefreshToken
import arun.pkg.sample_notes_spring_boot.database.model.User
import arun.pkg.sample_notes_spring_boot.database.repository.RefreshTokenRepository
import arun.pkg.sample_notes_spring_boot.database.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String) {
        val user = userRepository.findByEmail(email)
        if(user != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists!")
        }
        val hashedPassword = hashEncoder.encode(password)
        userRepository.save(User(email = email, hashedPassword = hashedPassword))
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email) ?: throw BadCredentialsException("Invalid credentials!")
        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials!")
        }
        val accessToken = jwtService.generateAccessToken(user.id.toHexString())
        val refreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        storeRefreshToken(user.id, refreshToken)
        return TokenPair(accessToken, refreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken))
            throw ResponseStatusException(HttpStatus.valueOf(401), "Invalid refresh token!")

        val userId =
            jwtService.getUserIdFromToken(refreshToken) ?: throw ResponseStatusException(
                HttpStatus.valueOf(401),
                "Invalid refresh token!"
            )
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ResponseStatusException(HttpStatus.valueOf(401), "Invalid refresh token!")
        }

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(
                HttpStatus.valueOf(401),
                "Refresh token not recognized(maybe used already)!"
            )

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        val accessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)
        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(accessToken, newRefreshToken)
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
        val hashed = hashToken(rawRefreshToken)
        val expiryMillis = jwtService.refreshTokenValidityMillis
        val expiresAt = Instant.now().plusMillis(expiryMillis)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}