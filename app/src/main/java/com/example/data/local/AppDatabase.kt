package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.WorkOrder
import com.example.data.model.ServiceRequest

@Database(entities = [WorkOrder::class, ServiceRequest::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
}
