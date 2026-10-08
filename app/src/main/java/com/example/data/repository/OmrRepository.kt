package com.example.data.repository

import com.example.data.db.OmrDao
import com.example.data.model.ExamEntity
import com.example.data.model.ResultEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

class OmrRepository(private val dao: OmrDao) {

    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allResults: Flow<List<ResultEntity>> = dao.getAllResults()

    fun getExamById(id: Long): Flow<ExamEntity?> = dao.getExamById(id)
    suspend fun getExamByIdSync(id: Long): ExamEntity? = dao.getExamByIdSync(id)

    suspend fun insertExam(exam: ExamEntity): Long = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)

    suspend fun getStudentByRoll(roll: String): StudentEntity? = dao.getStudentByRoll(roll)
    suspend fun insertStudent(student: StudentEntity) = dao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = dao.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity, deleteResults: Boolean = false) {
        if (deleteResults) {
            dao.deleteResultsForStudent(student.roll)
        }
        dao.deleteStudent(student)
    }

    fun getResultsForExam(examId: Long): Flow<List<ResultEntity>> = dao.getResultsForExam(examId)
    fun getResultsForStudent(roll: String): Flow<List<ResultEntity>> = dao.getResultsForStudent(roll)
    fun getResultById(id: Long): Flow<ResultEntity?> = dao.getResultById(id)
    suspend fun getExistingResult(examId: Long, roll: String): ResultEntity? = dao.getExistingResult(examId, roll)

    suspend fun insertResult(result: ResultEntity): Long = dao.insertResult(result)
    suspend fun updateResult(result: ResultEntity) = dao.updateResult(result)
    suspend fun deleteResult(result: ResultEntity) = dao.deleteResult(result)
}
