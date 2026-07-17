package com.keyfyndr.backend.common.ratelimit

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

/**
 * Redis-backed sliding-window rate limiter for the map-preview endpoint.
 *
 * Strategy: fixed-window counter keyed by `map_rate:{userId}` with a 1-minute TTL.
 * Each call increments the counter atomically; if the count exceeds the configured
 * limit, the request is rejected (HTTP 429 from the controller).
 *
 * Why Redis?
 *  - Already a dependency in this project (used for token/session data).
 *  - Atomic INCR + EXPIRE is lock-free and horizontally scalable.
 *  - If Redis is unavailable, [allowRequest] fails open (passes the request through)
 *    rather than blocking legitimate users — the trade-off is acceptable given the
 *    low-value nature of map images vs. user-facing data.
 */
@Component
class MapRateLimiter(
    private val redis: StringRedisTemplate,
    @Value("\${app.map.rate-limit.requests-per-minute:20}")
    private val requestsPerMinute: Long
) {
    private val logger = LoggerFactory.getLogger(MapRateLimiter::class.java)
    private val windowDuration: Duration = Duration.ofMinutes(1)

    /**
     * Returns `true` if the user is within the rate limit for this minute window,
     * `false` if the limit has been exceeded (caller should return HTTP 429).
     *
     * Fails open (returns `true`) on any Redis connectivity error.
     */
    fun allowRequest(userId: UUID): Boolean {
        return try {
            val key = "map_rate:$userId"
            val ops = redis.opsForValue()

            // Increment and get the new count atomically
            val count = ops.increment(key) ?: return true

            // Set TTL only on the first request in the window (count == 1)
            if (count == 1L) {
                redis.expire(key, windowDuration)
            }

            val allowed = count <= requestsPerMinute
            if (!allowed) {
                logger.warn("Rate limit exceeded for userId=$userId (count=$count, limit=$requestsPerMinute/min)")
            }
            allowed
        } catch (ex: Exception) {
            // Fail open — don't block users if Redis is temporarily unavailable
            logger.error("Redis rate limiter error for userId=$userId, failing open: ${ex.message}")
            true
        }
    }

    /**
     * Returns the remaining requests allowed for this user in the current window,
     * or [requestsPerMinute] if no counter exists yet. Used for X-RateLimit headers.
     */
    fun remainingRequests(userId: UUID): Long {
        return try {
            val count = redis.opsForValue().get("map_rate:$userId")?.toLongOrNull() ?: 0L
            maxOf(0L, requestsPerMinute - count)
        } catch (ex: Exception) {
            requestsPerMinute
        }
    }
}
