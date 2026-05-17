package com.example
import com.example.Data.DatabaseConnector
import com.example.plugins.configureHttp
import com.example.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*


fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 1. Включаем JSON-сериализацию
    install(ContentNegotiation) {
        json()
    }

    // 2. Подключаем HTTP-плагины (CORS, Compression, Headers)
    configureHttp()

    // 3. Подключаем маршруты
    configureRouting()

    // 4. (Позже раскомментируешь) Инициализация БД
     DatabaseConnector.connect()
     DatabaseConnector.createTables()
}