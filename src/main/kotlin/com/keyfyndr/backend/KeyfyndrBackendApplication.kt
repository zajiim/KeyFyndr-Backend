package com.keyfyndr.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KeyfyndrBackendApplication

fun main(args: Array<String>) {
    runApplication<KeyfyndrBackendApplication>(*args)
}
