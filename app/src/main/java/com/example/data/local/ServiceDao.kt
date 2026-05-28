package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkOrder
import com.example.data.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    // --- Work Orders ---
    @Query("SELECT * FROM work_orders ORDER BY timestamp DESC")
    fun getAllWorkOrders(): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE id = :id LIMIT 1")
    suspend fun getWorkOrderById(id: Int): WorkOrder?

    @Query("SELECT * FROM work_orders WHERE qrCodeData = :qrData LIMIT 1")
    suspend fun getWorkOrderByQr(qrData: String): WorkOrder?

    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY timestamp DESC")
    fun getWorkOrdersByStatus(status: String): Flow<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(order: WorkOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrders(orders: List<WorkOrder>)

    @Update
    suspend fun updateWorkOrder(order: WorkOrder)

    @Query("UPDATE work_orders SET status = :status, paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updateOrderStatusAndPayment(id: Int, status: String, paymentStatus: String)

    @Query("DELETE FROM work_orders WHERE id = :id")
    suspend fun deleteWorkOrderById(id: Int)

    // --- Service Requests ---
    @Query("SELECT * FROM service_requests ORDER BY timestamp DESC")
    fun getAllServiceRequests(): Flow<List<ServiceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRequest(request: ServiceRequest): Long

    @Query("DELETE FROM service_requests WHERE id = :id")
    suspend fun deleteServiceRequestById(id: Int)
}
