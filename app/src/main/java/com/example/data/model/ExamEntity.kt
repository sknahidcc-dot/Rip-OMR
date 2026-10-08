package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: String,
    val totalQuestions: Int = 100,
    val passPercent: Double = 80.0,
    val negativeMark: Double = 0.25,
    val answers: String = "", // e.g., "1234..." where 1=ক, 2=খ, 3=গ, 4=ঘ, 0=not set
    val createdAt: Long = System.currentTimeMillis()
)
