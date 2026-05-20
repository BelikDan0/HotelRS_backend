package com.example

import com.example.Data.DatabaseConnector
import com.example.plugins.configureHttp
import com.example.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // 1. Включаем JSON-сериализацию с настройками
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 2. Подключаем HTTP-плагины (CORS, Compression, Headers)
    configureHttp()

    // 3. Подключаем маршруты
    configureRouting()

    // 4. Инициализация БД
    DatabaseConnector.connect()
    DatabaseConnector.createTables()
}