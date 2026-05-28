package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceType: String, // Albañilería, Electricidad, Pintura, Fontanería
    val description: String,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String,
    val urgency: String, // "Alta", "Media", "Baja"
    val preferredDate: String,
    val address: String,
    val status: String = "Pendiente de Presupuesto",
    val timestamp: Long = System.currentTimeMillis()
)
