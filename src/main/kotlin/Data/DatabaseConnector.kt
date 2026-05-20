package com.example.Data



import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table



import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnector {
    fun connect() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/database_number_hotel",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "123" // <-- ЗАМЕНИ НА СВОЙ ПАРОЛЬ ОТ PG
        )
    }

    fun createTables() {
        transaction {
            SchemaUtils.create(Guests, Staff, Categories, Tickets)
        }
    }
}

// Таблицы (соответствуют твоему init.sql)
object Guests : Table("guests") {
    val id = integer("id").autoIncrement()
    val fullName = varchar("full_name", 100)
    val phone = varchar("phone", 20)
    val roomNumber = varchar("room_number", 10)
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id, name = "PK_guests_id")
}

object Staff : Table("staff") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20)
    override val primaryKey = PrimaryKey(id, name = "PK_staff_id")
}

object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id, name = "PK_categories_id")
}

object Tickets : Table("tickets") {
    val id = integer("id").autoIncrement()
    val guestId = integer("guest_id").references(Guests.id)
    val categoryId = integer("category_id").references(Categories.id)
    val description = text("description")
    val status = varchar("status", 20)
    val createdAt = varchar("created_at", 50)
    val updatedAt = varchar("updated_at", 50)
    val assignedStaffId = integer("assigned_staff_id").nullable()
    override val primaryKey = PrimaryKey(id, name = "PK_tickets_id")
}