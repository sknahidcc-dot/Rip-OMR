package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.PointF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ExamEntity
import com.example.data.model.ResultEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.OmrRepository
import com.example.omr.OmrScanEvaluation
import com.example.omr.OmrScannerEngine
import com.example.omr.OmrSheetGenerator
import com.example.util.QrUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object Exams : Screen()
    data class ExamEdit(val examId: Long? = null) : Screen()
    data class ExamQrShare(val exam: ExamEntity) : Screen()
    data object Students : Screen()
    data class StudentDetail(val roll: String) : Screen()
    data object Analytics : Screen()
    data object Settings : Screen()
    data object Scanner : Screen()
    data object Verification : Screen()
    data class ResultDetail(val resultId: Long) : Screen()
    data object PrintableSheet : Screen()
}

class OmrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OmrRepository = OmrRepository(
        AppDatabase.getInstance(application).omrDao()
    )

    // Navigation stack
    private val _screenState = MutableStateFlow<Screen>(Screen.Splash)
    val screenState: StateFlow<Screen> = _screenState.asStateFlow()

    // Database reactive streams
    val exams: StateFlow<List<ExamEntity>> = repository.allExams.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val students: StateFlow<List<StudentEntity>> = repository.allStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val results: StateFlow<List<ResultEntity>> = repository.allResults.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Selected Exam for scanning
    private val _selectedExam = MutableStateFlow<ExamEntity?>(null)
    val selectedExam: StateFlow<ExamEntity?> = _selectedExam.asStateFlow()

    // Scanning & Evaluation state
    private val _currentEvaluation = MutableStateFlow<OmrScanEvaluation?>(null)
    val currentEvaluation: StateFlow<OmrScanEvaluation?> = _currentEvaluation.asStateFlow()

    private val _isProcessingScan = MutableStateFlow(false)
    val isProcessingScan: StateFlow<Boolean> = _isProcessingScan.asStateFlow()

    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    val scanErrorMessage: StateFlow<String?> = _scanErrorMessage.asStateFlow()

    // Settings
    val sensitivity = MutableStateFlow(0.68f)
    val useBengaliDigits = MutableStateFlow(false)
    val allowNegativeTotal = MutableStateFlow(false)

    // Conflict dialog when saving result
    private val _existingResultPrompt = MutableStateFlow<ResultEntity?>(null)
    val existingResultPrompt: StateFlow<ResultEntity?> = _existingResultPrompt.asStateFlow()

    init {
        // Populate sample initial exam & students if database is empty on first launch
        viewModelScope.launch(Dispatchers.IO) {
            preloadInitialDataIfEmpty()
        }
    }

    private suspend fun preloadInitialDataIfEmpty() {
        val sampleExam = repository.getExamByIdSync(1)
        if (sampleExam == null) {
            // Generate a sample 100-question answer key pattern: "12341234..."
            val sampleAnswers = StringBuilder()
            for (i in 1..100) {
                sampleAnswers.append((((i - 1) % 4) + 1).toString())
            }

            val examId = repository.insertExam(
                ExamEntity(
                    id = 1,
                    name = "মডেল টেস্ট - ০১ (বিসিএস/ভর্তি পরীক্ষা)",
                    date = "২০২৬-১০-০৮",
                    totalQuestions = 100,
                    passPercent = 80.0,
                    negativeMark = 0.25,
                    answers = sampleAnswers.toString()
                )
            )

            // Seed sample students
            repository.insertStudent(StudentEntity(roll = "1001", name = "রাকিবুল ইসলাম", phone = "01711000001"))
            repository.insertStudent(StudentEntity(roll = "1002", name = "ফারহানা আক্তার", phone = "01711000002"))
            repository.insertStudent(StudentEntity(roll = "1248", name = "নাহিদ হাসান", phone = "01711000003"))
            repository.insertStudent(StudentEntity(roll = "1004", name = "সাকিব আল হাসান", phone = "01711000004"))

            _selectedExam.value = repository.getExamByIdSync(examId)
        }
    }

    fun navigateTo(screen: Screen) {
        _screenState.value = screen
    }

    fun selectExam(exam: ExamEntity) {
        _selectedExam.value = exam
    }

    fun clearScanError() {
        _scanErrorMessage.value = null
    }

    // --- Exam Operations ---
    fun saveExam(exam: ExamEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (exam.id == 0L) {
                repository.insertExam(exam)
            } else {
                repository.updateExam(exam)
                exam.id
            }
            if (_selectedExam.value?.id == id || _selectedExam.value == null) {
                _selectedExam.value = repository.getExamByIdSync(id)
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExam(exam)
            if (_selectedExam.value?.id == exam.id) {
                _selectedExam.value = exams.value.firstOrNull { it.id != exam.id }
            }
        }
    }

    fun duplicateExam(exam: ExamEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val duplicate = exam.copy(
                id = 0,
                name = "${exam.name} (কপি)",
                createdAt = System.currentTimeMillis()
            )
            repository.insertExam(duplicate)
        }
    }

    fun importExamFromQr(jsonStr: String): Boolean {
        val imported = QrUtils.decodeExamFromJson(jsonStr) ?: return false
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertExam(imported)
            _selectedExam.value = repository.getExamByIdSync(id)
        }
        return true
    }

    // --- Student Operations ---
    fun saveStudent(student: StudentEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertStudent(student)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteStudent(student: StudentEntity, deleteResults: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudent(student, deleteResults)
        }
    }

    // --- OMR Scanning Process ---
    fun scanImage(bitmap: Bitmap, customCorners: List<PointF>? = null) {
        val exam = _selectedExam.value ?: exams.value.firstOrNull()
        if (exam == null) {
            _scanErrorMessage.value = "অনুগ্রহ করে স্ক্যান করার আগে একটি পরীক্ষা নির্বাচন বা তৈরি করুন।"
            return
        }

        _isProcessingScan.value = true
        _scanErrorMessage.value = null

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val evaluation = OmrScannerEngine.evaluateSheet(
                    bitmap = bitmap,
                    exam = exam,
                    corners = customCorners,
                    sensitivity = sensitivity.value,
                    allowNegativeTotal = allowNegativeTotal.value
                )

                _currentEvaluation.value = evaluation
                withContext(Dispatchers.Main) {
                    _isProcessingScan.value = false
                    _screenState.value = Screen.Verification
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isProcessingScan.value = false
                    _scanErrorMessage.value = "ওএমআর শিট বিশ্লেষণ ব্যর্থ হয়েছে: ${e.localizedMessage ?: "চিত্রটি স্পষ্ট ও সোজা রাখুন"}"
                }
            }
        }
    }

    fun testWithSampleSheet(fillSample: Boolean = true, sampleRoll: String = "1248") {
        viewModelScope.launch(Dispatchers.Default) {
            _isProcessingScan.value = true
            val sampleBitmap = OmrSheetGenerator.generateSheet(
                scaleDpi = 150,
                fillSampleAnswers = fillSample,
                sampleRoll = sampleRoll
            )
            scanImage(sampleBitmap)
        }
    }

    fun updateRollInCurrentScan(newRoll: String) {
        val current = _currentEvaluation.value ?: return
        _currentEvaluation.value = current.copy(
            roll = newRoll,
            isRollComplete = newRoll.length == 4 && !newRoll.contains("?")
        )
    }

    fun checkAndSaveResult(
        onSuccess: (Long) -> Unit,
        onRollPromptNeeded: (String) -> Unit
    ) {
        val eval = _currentEvaluation.value ?: return
        val currentRoll = eval.roll

        if (currentRoll.length != 4 || currentRoll.contains("?")) {
            onRollPromptNeeded(currentRoll)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Check student existence
            var student = repository.getStudentByRoll(currentRoll)
            if (student == null) {
                // Auto create student
                student = StudentEntity(
                    roll = currentRoll,
                    name = "শিক্ষার্থী-$currentRoll"
                )
                repository.insertStudent(student)
            }

            // Check if student already has a result for this exam
            val existing = repository.getExistingResult(eval.examId, currentRoll)
            if (existing != null) {
                _existingResultPrompt.value = existing
            } else {
                val resultId = persistResult(eval, student.name)
                withContext(Dispatchers.Main) {
                    onSuccess(resultId)
                }
            }
        }
    }

    fun confirmOverwriteExistingResult(onSuccess: (Long) -> Unit) {
        val eval = _currentEvaluation.value ?: return
        val existing = _existingResultPrompt.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val student = repository.getStudentByRoll(eval.roll)
            val studentName = student?.name ?: "শিক্ষার্থী-${eval.roll}"
            val updated = ResultEntity(
                id = existing.id,
                examId = eval.examId,
                examName = eval.examTitle,
                roll = eval.roll,
                studentName = studentName,
                correct = eval.correctCount,
                wrong = eval.wrongCount,
                unanswered = eval.unansweredCount,
                totalMarks = eval.totalMarks,
                negativeTotal = eval.negativeMarks,
                netMarks = eval.netMarks,
                percentage = eval.percentage,
                passed = eval.isPassed,
                answersGiven = eval.questions.joinToString(",") { it.detectedOptions.joinToString("+") },
                scannedAt = System.currentTimeMillis()
            )
            repository.updateResult(updated)
            _existingResultPrompt.value = null
            withContext(Dispatchers.Main) {
                onSuccess(existing.id)
            }
        }
    }

    fun confirmKeepBothResults(onSuccess: (Long) -> Unit) {
        val eval = _currentEvaluation.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val student = repository.getStudentByRoll(eval.roll)
            val studentName = student?.name ?: "শিক্ষার্থী-${eval.roll}"
            val resultId = persistResult(eval, studentName)
            _existingResultPrompt.value = null
            withContext(Dispatchers.Main) {
                onSuccess(resultId)
            }
        }
    }

    fun cancelResultConflict() {
        _existingResultPrompt.value = null
    }

    private suspend fun persistResult(eval: OmrScanEvaluation, studentName: String): Long {
        val entity = ResultEntity(
            examId = eval.examId,
            examName = eval.examTitle,
            roll = eval.roll,
            studentName = studentName,
            correct = eval.correctCount,
            wrong = eval.wrongCount,
            unanswered = eval.unansweredCount,
            totalMarks = eval.totalMarks,
            negativeTotal = eval.negativeMarks,
            netMarks = eval.netMarks,
            percentage = eval.percentage,
            passed = eval.isPassed,
            answersGiven = eval.questions.joinToString(",") { it.detectedOptions.joinToString("+") },
            scannedAt = System.currentTimeMillis()
        )
        return repository.insertResult(entity)
    }

    fun deleteResult(result: ResultEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteResult(result)
        }
    }
}
