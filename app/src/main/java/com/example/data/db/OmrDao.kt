package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExamEntity
import com.example.data.model.ResultEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OmrDao {

    // --- Exams ---
    @Query("SELECT * FROM exams ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    fun getExamById(id: Long): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamByIdSync(id: Long): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY roll ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE roll = :roll LIMIT 1")
    suspend fun getStudentByRoll(roll: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    // --- Results ---
    @Query("SELECT * FROM results ORDER BY scannedAt DESC")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE examId = :examId ORDER BY netMarks DESC, correct DESC")
    fun getResultsForExam(examId: Long): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE roll = :roll ORDER BY scannedAt DESC")
    fun getResultsForStudent(roll: String): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE id = :id LIMIT 1")
    fun getResultById(id: Long): Flow<ResultEntity?>

    @Query("SELECT * FROM results WHERE examId = :examId AND roll = :roll LIMIT 1")
    suspend fun getExistingResult(examId: Long, roll: String): ResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity): Long

    @Update
    suspend fun updateResult(result: ResultEntity)

    @Delete
    suspend fun deleteResult(result: ResultEntity)

    @Query("DELETE FROM results WHERE roll = :roll")
    suspend fun deleteResultsForStudent(roll: String)
}
