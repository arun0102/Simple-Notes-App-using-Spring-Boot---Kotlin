package arun.pkg.sample_notes_spring_boot.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

    private val accessTokenValidityMillis = 15 * 60 * 1000L
    val refreshTokenValidityMillis = 30 * 24 * 60 * 60 * 1000L

    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, "access", accessTokenValidityMillis)
    }

    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, "refresh", refreshTokenValidityMillis)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    // Authorization: Bearer <token>
    fun getUserIdFromToken(token: String): String? {
        val claims = parseAllClaims(token) ?: throw ResponseStatusException(HttpStatus.valueOf(401), "Invalid token!")
        return claims.subject
    }


    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) token.removePrefix("Bearer ") else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}