package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Laporan
import com.example.data.Siswa
import com.example.data.SiswaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SiswaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SiswaRepository(application)

    val currentTeacherName = repository.currentTeacherName

    // All students
    val students: StateFlow<List<Siswa>> = repository.getAllStudentsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current app date (YYYY-MM-DD)
    private val _currentDate = MutableStateFlow(getTodayDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // All reports for today
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayReports: StateFlow<List<Laporan>> = _currentDate
        .flatMapLatest { date -> repository.getLaporanForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected Student for detailed viewing
    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    // Flow representing selected student profile
    private val _selectedStudent = MutableStateFlow<Siswa?>(null)
    val selectedStudent: StateFlow<Siswa?> = _selectedStudent.asStateFlow()

    // Flow representing chosen student for Student Role login
    private val _currentStudentId = MutableStateFlow<Long?>(null)
    val currentStudentId: StateFlow<Long?> = _currentStudentId.asStateFlow()

    private val _currentStudent = MutableStateFlow<Siswa?>(null)
    val currentStudent: StateFlow<Siswa?> = _currentStudent.asStateFlow()

    // Selected student reports history
    private val _selectedStudentHistory = MutableStateFlow<List<Laporan>>(emptyList())
    val selectedStudentHistory: StateFlow<List<Laporan>> = _selectedStudentHistory.asStateFlow()

    // Current student reports history (own history)
    private val _currentStudentHistory = MutableStateFlow<List<Laporan>>(emptyList())
    val currentStudentHistory: StateFlow<List<Laporan>> = _currentStudentHistory.asStateFlow()

    // Today's report for the active logged-in student
    private val _currentStudentTodayReport = MutableStateFlow<Laporan?>(null)
    val currentStudentTodayReport: StateFlow<Laporan?> = _currentStudentTodayReport.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database at first run
            repository.checkAndSeedDatabase()
        }
    }

    // Set active student for viewing details as Teacher
    fun selectStudent(studentId: Long) {
        _selectedStudentId.value = studentId
        viewModelScope.launch {
            val s = repository.getStudentById(studentId)
            _selectedStudent.value = s
            if (s != null) {
                repository.getLaporanForStudent(studentId).collect {
                    _selectedStudentHistory.value = it
                }
            }
        }
    }

    // Set active student logging in
    fun loginAsStudent(studentId: Long) {
        _currentStudentId.value = studentId
        viewModelScope.launch {
            val s = repository.getStudentById(studentId)
            _currentStudent.value = s
            if (s != null) {
                // Collect history
                launch {
                    repository.getLaporanForStudent(studentId).collect {
                        _currentStudentHistory.value = it
                    }
                }
                // Check if today's report exists
                launch {
                    checkTodayReport(studentId)
                }
            }
        }
    }

    private suspend fun checkTodayReport(studentId: Long) {
        val todayRep = repository.getLaporanForStudentAndDate(studentId, currentDate.value)
        _currentStudentTodayReport.value = todayRep
    }

    // Add student (Admin function)
    fun addStudent(name: String, grade: Int, className: String, nisn: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addStudent(name, grade, className, nisn)
            onSuccess()
        }
    }

    // Submit or Edit Daily Report (Student function)
    fun submitReport(
        attendance: String,
        condition: String,
        homeActivity: String,
        schoolActivity: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val sId = _currentStudentId.value ?: return
        viewModelScope.launch {
            val existing = repository.getLaporanForStudentAndDate(sId, currentDate.value)
            
            val newReport = Laporan(
                id = existing?.id ?: 0,
                studentId = sId,
                date = currentDate.value,
                timestamp = System.currentTimeMillis(),
                attendanceStatus = attendance,
                conditionStatus = condition,
                homeActivity = homeActivity,
                schoolActivity = schoolActivity,
                notes = notes,
                // keep old teacher comment if editing
                teacherComment = existing?.teacherComment ?: ""
            )

            repository.insertOrUpdateLaporan(newReport)
            checkTodayReport(sId)
            onSuccess()
        }
    }

    // Add feedback comment (Admin function)
    fun addTeacherComment(laporanId: Long, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateTeacherComment(laporanId, comment)
            // Reload history to reflect the updated note
            _selectedStudentId.value?.let { selectStudent(it) }
            onSuccess()
        }
    }

    // Helpers
    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun getFormattedDisplayDate(dateStr: String): String {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(dateStr) ?: return dateStr
            val sdfOutput = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            return sdfOutput.format(date)
        } catch (e: Exception) {
            return dateStr
        }
    }
}
