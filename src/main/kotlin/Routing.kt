package com.example.plugins

import com.example.Data.AuthResponse
import com.example.Data.CreateStaffRequest
import com.example.Data.CreateTicketRequest
import com.example.Data.LoginRequest
import com.example.Data.TicketDto
import com.example.Data.UpdateStatusRequest
import com.example.Data.Categories
import com.example.Data.Guests
import com.example.Data.Staff
import com.example.Data.Tickets
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ... остальной код

@Serializable
data class TicketResponse(
    val id: Int,
    val guestName: String,
    val roomNumber: String,
    val categoryName: String,
    val description: String,
    val status: String,
    val createdAt: String
)

fun Application.configureRouting() {
    routing {
        // Проверка работоспособности
        staticResources("/", "static", index = "login.html")
        get("/") {
            call.respondRedirect("/login.html")
        }

        post("/api/admin/staff") {
            try {
                // Явно читаем body как строку и парсим
                val body = call.receiveText()
                println("Received body: $body") // Для отладки

                val req = Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<CreateStaffRequest>(body)

                val exists = transaction {
                    Staff.select { Staff.username eq req.username }.firstOrNull()
                }

                if (exists != null) {
                    call.respond(HttpStatusCode.Conflict, "User already exists")
                    return@post
                }

                transaction {
                    Staff.insert {
                        it[username] = req.username
                        it[passwordHash] = req.password // В реальном проекте хешируйте!
                        it[role] = req.role
                    }
                }

                call.respond(HttpStatusCode.Created, mapOf("message" to "Staff created successfully"))
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        // Авторизация
        post("/api/auth/login") {
            val req = call.receive<LoginRequest>()

            val response = transaction {
                if (req.type == "GUEST") {
                    val guest = Guests.select { Guests.phone eq req.identifier }.firstOrNull()
                    if (guest != null) {
                        // Возвращаем data class вместо Map
                        AuthResponse(
                            token = "guest_${guest[Guests.id]}",
                            role = "GUEST",
                            guestId = guest[Guests.id]
                        )
                    } else null
                } else {
                    val staff = Staff.select {
                        (Staff.username eq req.identifier) and
                                (Staff.passwordHash eq (req.password ?: ""))
                    }.firstOrNull()

                    if (staff != null) {
                        // Возвращаем data class вместо Map
                        AuthResponse(
                            token = "staff_${staff[Staff.id]}",
                            role = staff[Staff.role],
                            guestId = null
                        )
                    } else null
                }
            }

            if (response != null) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        // Создание заявки (для гостя)
        post("/api/tickets") {
            val req = call.receive<CreateTicketRequest>()
            val guestId = call.request.headers["X-Guest-Id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing guest ID")

            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            transaction {
                Tickets.insert {
                    it[Tickets.guestId] = guestId
                    it[Tickets.categoryId] = req.categoryId
                    it[Tickets.description] = req.description
                    it[Tickets.status] = "NEW"
                    it[Tickets.createdAt] = now
                    it[Tickets.updatedAt] = now
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("message" to "Ticket created successfully"))
        }

        // Получение заявок гостя
        get("/api/guest/tickets") {
            val guestId = call.request.headers["X-Guest-Id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing guest ID")

            val tickets: List<TicketResponse> = transaction {
                (Tickets innerJoin Guests innerJoin Categories)
                    .select { Tickets.guestId eq guestId }
                    .orderBy(Tickets.createdAt to SortOrder.DESC)
                    .map { row ->
                        TicketResponse(
                            id = row[Tickets.id],
                            guestName = row[Guests.fullName],
                            roomNumber = row[Guests.roomNumber],
                            categoryName = row[Categories.name],
                            description = row[Tickets.description],
                            status = row[Tickets.status],
                            createdAt = row[Tickets.createdAt]
                        )
                    }
            }

            call.respond(tickets)
        }

        // Получение всех заявок (для админа)
        get("/api/admin/tickets") {
            val statusFilter = call.request.queryParameters["status"]

            val tickets: List<TicketResponse> = transaction {
                val query = (Tickets innerJoin Guests innerJoin Categories)
                val select = if (statusFilter != null) {
                    query.select { Tickets.status eq statusFilter }
                } else {
                    query.selectAll()
                }

                select.orderBy(Tickets.createdAt to SortOrder.DESC)
                    .map { row ->
                        TicketResponse(
                            id = row[Tickets.id],
                            guestName = row[Guests.fullName],
                            roomNumber = row[Guests.roomNumber],
                            categoryName = row[Categories.name],
                            description = row[Tickets.description],
                            status = row[Tickets.status],
                            createdAt = row[Tickets.createdAt]
                        )
                    }
            }

            call.respond(tickets)
        }

        // Обновление статуса заявки
        put("/api/tickets/{id}/status") {
            val ticketId = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ticket ID")

            val req = call.receive<Map<String, String>>()
            val newStatus = req["status"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing status")

            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            transaction {
                Tickets.update({ Tickets.id eq ticketId }) {
                    it[status] = newStatus
                    it[updatedAt] = now
                }
            }

            call.respond(mapOf("message" to "Status updated successfully"))
        }
    }
}