package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "results",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("examId"),
        Index("roll")
    ]
)
data class ResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val examName: String,
    val roll: String,
    val studentName: String,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val totalMarks: Double,
    val negativeTotal: Double,
    val netMarks: Double,
    val percentage: Double,
    val passed: Boolean,
    val answersGiven: String, // String representation of answers detected (e.g. 1,2,0,3...)
    val scannedAt: Long = System.currentTimeMillis()
)
