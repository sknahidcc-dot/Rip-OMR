package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val roll: String, // 4-digit unique roll, e.g. "0123"
    val name: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
