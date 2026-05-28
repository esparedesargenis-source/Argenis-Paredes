package com.example.data.repository

import com.example.data.local.ServiceDao
import com.example.data.model.WorkOrder
import com.example.data.model.ServiceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class ServiceRepository(private val serviceDao: ServiceDao) {

    val allWorkOrders: Flow<List<WorkOrder>> = serviceDao.getAllWorkOrders()
    val allServiceRequests: Flow<List<ServiceRequest>> = serviceDao.getAllServiceRequests()

    fun getWorkOrdersByStatus(status: String): Flow<List<WorkOrder>> {
        return serviceDao.getWorkOrdersByStatus(status)
    }

    suspend fun getWorkOrderById(id: Int): WorkOrder? = withContext(Dispatchers.IO) {
        serviceDao.getWorkOrderById(id)
    }

    suspend fun getWorkOrderByQr(qrData: String): WorkOrder? = withContext(Dispatchers.IO) {
        serviceDao.getWorkOrderByQr(qrData)
    }

    suspend fun insertWorkOrder(order: WorkOrder): Long = withContext(Dispatchers.IO) {
        serviceDao.insertWorkOrder(order)
    }

    suspend fun updateWorkOrder(order: WorkOrder) = withContext(Dispatchers.IO) {
        serviceDao.updateWorkOrder(order)
    }

    suspend fun updateOrderStatusAndPayment(id: Int, status: String, paymentStatus: String) = withContext(Dispatchers.IO) {
        serviceDao.updateOrderStatusAndPayment(id, status, paymentStatus)
    }

    suspend fun deleteWorkOrder(id: Int) = withContext(Dispatchers.IO) {
        serviceDao.deleteWorkOrderById(id)
    }

    suspend fun insertServiceRequest(request: ServiceRequest): Long = withContext(Dispatchers.IO) {
        serviceDao.insertServiceRequest(request)
    }

    suspend fun deleteServiceRequest(id: Int) = withContext(Dispatchers.IO) {
        serviceDao.deleteServiceRequestById(id)
    }

    suspend fun prepopulateDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingOrders = serviceDao.getAllWorkOrders().firstOrNull()
        if (existingOrders.isNullOrEmpty()) {
            val defaultOrders = listOf(
                WorkOrder(
                    title = "Remodelación de Cocina Moderna",
                    description = "Renovación integral de cocina residencial: colocación de pisos porcelánicos premium, revestimiento estético de paredes, instalación de grifería monomando y montaje de encimera de granito natural.",
                    serviceType = "Albañilería",
                    status = "Pendiente",
                    clientName = "Carlos Mendoza",
                    subtotalLabor = 1200.00,
                    subtotalMaterials = 1650.00,
                    totalPrice = 2850.00,
                    paymentStatus = "Pendiente",
                    qrCodeData = "AP-ORDER-101",
                    materialsDetail = "Piso Porcelanato Gris 60x60cm (45m²), Cemento Portland Gris (12 sacos), Pegamento cerámico de alta adherencia, Encimera de Granito Blanco Polar (4 metros lineales), Boquilla especial con sellador.",
                    laborDetail = "Remoción técnica de azulejos obsoletos, nivelado y compactado de contrapiso, asentado simétrico de porcelanato, corte y montaje milimétrico de encimera de granito, e instalación de grifería.",
                    address = "Avenida 27 de Febrero, Edificio Las Palmas #3B"
                ),
                WorkOrder(
                    title = "Distribución Eléctrica Certificada",
                    description = "Rediseño completo de la red eléctrica en segunda planta. Reubicación del tablero principal, balance de cargas y montaje de puntos de iluminación LED de alta eficiencia energética.",
                    serviceType = "Electricidad",
                    status = "En Progreso",
                    clientName = "Dra. Lucía Almonte",
                    subtotalLabor = 850.00,
                    subtotalMaterials = 730.00,
                    totalPrice = 1580.00,
                    paymentStatus = "Pendiente",
                    qrCodeData = "AP-ORDER-102",
                    materialsDetail = "Cable Thhn Calibre 12 AWG (3 rollos), Panel de Disyuntores de 12 circuitos, Interruptores termomagnéticos Square D (20A, 15A), Spotlights LED Empotrables 9W dimerizables (15 unidades), Caja de paso de PVC.",
                    laborDetail = "Trazo y ranurado de muros, canalización estructurada con PVC conduit, cableado, empalmes estancos aislados, balanceado de fases en tablero termomagnético e instalación de focos dicroicos.",
                    address = "Calle Doctor Delgado, Residencial Los Girasoles, Casa #14"
                ),
                WorkOrder(
                    title = "Impermeabilización y Revestimiento",
                    description = "Tratamiento impermeabilizante definitivo en pared exterior para prevenir la humedad severa del trópico, seguido del acabado con pintura elastomérica de calidad arquitectónica superior.",
                    serviceType = "Pintura",
                    status = "Completado",
                    clientName = "Consorcio Inmobiliario Colonial",
                    subtotalLabor = 1500.00,
                    subtotalMaterials = 980.00,
                    totalPrice = 2480.00,
                    paymentStatus = "Pagado",
                    qrCodeData = "AP-ORDER-103",
                    materialsDetail = "Pintura Elastomérica Profesional Lanco Impercoat (8 Cubetas), Masilla acrílica de alta elasticidad para juntas dilatables, Sellador bloqueador de alcalinidad, Malla de refuerzo de fibra de vidrio.",
                    laborDetail = "Lavado hidrodinámico exhaustivo a presión, remoción manual de revoque en mal estado, reparación estructural de fisuras mediante masilla elastomérica, aplicación de triple capa protectora.",
                    address = "Calle Isabel la Católica #154, Zona Colonial"
                ),
                WorkOrder(
                    title = "Optimización de Conexiones Hidráulicas",
                    description = "Detección rápida de fisura interna en red sanitaria, re-acondicionamiento de tuberías cruzadas mediante tecnología de termofusión para garantizar la máxima presión y cero fugas.",
                    serviceType = "Fontanería",
                    status = "Pendiente",
                    clientName = "Ing. Manuel Ruiz",
                    subtotalLabor = 450.00,
                    subtotalMaterials = 290.00,
                    totalPrice = 740.00,
                    paymentStatus = "Pendiente",
                    qrCodeData = "AP-ORDER-104",
                    materialsDetail = "Tubería PPR de termofusión de 1/2 y 3/4 pulgada, Codos, Tés y coples de termofusión, Válvulas esféricas de bronce de paso rápido, Mezcladora monomando de tina marca Helvex.",
                    laborDetail = "Ubicación técnica de la fuga, demolición limpia del muro afectado, tendido de tuberías de termofusión para agua fría/caliente, prueba de presión hidráulica estática y montaje de mezcladora.",
                    address = "Residencial Alameda, Bloque D, Manzana 6"
                )
            )
            serviceDao.insertWorkOrders(defaultOrders)
        }
    }
}

