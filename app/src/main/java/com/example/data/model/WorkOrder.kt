package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_orders")
data class WorkOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val serviceType: String, // Albañilería, Electricidad, Pintura, Fontanería
    val status: String, // "Pendiente", "En Progreso", "Completado"
    val clientName: String,
    val subtotalLabor: Double,
    val subtotalMaterials: Double,
    val totalPrice: Double,
    val paymentStatus: String, // "Pendiente", "Pagado"
    val qrCodeData: String, // e.g. "AP-ORDER-101"
    val materialsDetail: String, // e.g. "Cemento Gris x15 sacos ($120.00), Varilla 3/8 x10 clavos ($85.00)"
    val laborDetail: String, // e.g. "Preparación de superficie, vaciado de zapata, nivelación técnica"
    val address: String, // e.g. "Calle Principal #124, Zona Norte"
    val timestamp: Long = System.currentTimeMillis()
)
