package com.keyfyndr.backend.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

/**
 * Initializes the Firebase Admin SDK at application startup.
 *
 * ## Credential resolution order
 *
 * ### Local Development
 * Place `service-account.json` (downloaded from Firebase Console →
 * Project Settings → Service Accounts → Generate new private key) inside
 * `src/main/resources/`. The file is gitignored and will be picked up
 * automatically via the `app.fcm.service-account-path` property.
 *
 * ### Production / CI
 * Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the
 * **absolute path** of the service account JSON file on the server.
 * The `app.fcm.service-account-path` property can be left pointing to a
 * non-existent classpath file; the SDK will fall back to ADC automatically.
 *
 * ```
 * export GOOGLE_APPLICATION_CREDENTIALS=/secrets/keyfyndr-service-account.json
 * ```
 *
 * **Never commit service-account.json to version control.**
 */
@Configuration
class FirebaseConfig(
    @Value("\${app.fcm.service-account-path:service-account.json}")
    private val firebaseServiceAccountPath: String
) {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isNotEmpty()) {
            logger.info("Firebase already initialized, skipping.")
            return
        }

        try {
            val resource = ClassPathResource(firebaseServiceAccountPath)
            val credentials = if (resource.exists()) {
                logger.info("Loading Firebase credentials from classpath: $firebaseServiceAccountPath")
                GoogleCredentials.fromStream(resource.inputStream)
            } else {
                logger.info("Service account file not found at '$firebaseServiceAccountPath'. Falling back to Application Default Credentials.")
                GoogleCredentials.getApplicationDefault()
            }

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin SDK initialized successfully.")
        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase Admin SDK: ${e.message}. Push notifications will be disabled.", e)
        }
    }
}
