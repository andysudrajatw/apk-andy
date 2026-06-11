package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SiswaRepository(private val context: Context) {

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "pantau_perwalian_db"
        ).build()
    }

    private val dao: SiswaDao by lazy { db.siswaDao() }

    // Teacher / Advisor name who is the current admin user
    val currentTeacherName = "Drs. Budi Santoso"

    fun getAllStudentsFlow(): Flow<List<Siswa>> = dao.getAllStudentsFlow()

    fun getStudentByIdFlow(id: Long): Flow<Siswa?> = dao.getStudentByIdFlow(id)

    suspend fun getStudentById(id: Long): Siswa? = dao.getStudentById(id)

    suspend fun addStudent(name: String, grade: Int, className: String, nisn: String): Long {
        val newSiswa = Siswa(
            name = name,
            grade = grade,
            className = className,
            nisn = nisn,
            advisorName = currentTeacherName
        )
        return dao.insertStudent(newSiswa)
    }

    suspend fun updateStudent(siswa: Siswa) = dao.updateStudent(siswa)

    suspend fun deleteStudent(siswa: Siswa) = dao.deleteStudent(siswa)

    fun getLaporanForStudent(studentId: Long): Flow<List<Laporan>> = dao.getLaporanForStudentFlow(studentId)

    fun getLaporanForDate(date: String): Flow<List<Laporan>> = dao.getLaporanForDateFlow(date)

    suspend fun getLaporanForStudentAndDate(studentId: Long, date: String): Laporan? =
        dao.getLaporanForStudentAndDate(studentId, date)

    suspend fun insertOrUpdateLaporan(laporan: Laporan): Long {
        return dao.insertOrUpdateLaporan(laporan)
    }

    suspend fun updateTeacherComment(laporanId: Long, comment: String) {
        dao.updateTeacherComment(laporanId, comment)
    }

    fun getAllLaporanFlow(): Flow<List<Laporan>> = dao.getAllLaporanFlow()

    // Initialize mock data if empty
    suspend fun checkAndSeedDatabase() {
        val currentStudents = dao.getAllStudentsFlow().first()
        if (currentStudents.isEmpty()) {
            // Seed Students
            val students = listOf(
                Siswa(name = "Ahmad Rifai", grade = 10, className = "X MIPA 1", nisn = "1029384756", advisorName = currentTeacherName),
                Siswa(name = "Siti Rahmawati", grade = 10, className = "X MIPA 1", nisn = "1029485761", advisorName = currentTeacherName),
                Siswa(name = "Budi Wijaya", grade = 11, className = "XI IPS 2", nisn = "1120394857", advisorName = currentTeacherName),
                Siswa(name = "Dewi Lestari", grade = 11, className = "XI IPS 2", nisn = "1120495862", advisorName = currentTeacherName),
                Siswa(name = "Eko Prasetyo", grade = 12, className = "XII Bahasa", nisn = "1220596873", advisorName = currentTeacherName)
            )

            val studentIds = mutableListOf<Long>()
            for (student in students) {
                val id = dao.insertStudent(student)
                studentIds.add(id)
            }

            // Prepare local date strings for yesterday and today
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            val cal = Calendar.getInstance()
            val todayStr = dateFormat.format(cal.time)
            val todayMs = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)
            val yesterdayMs = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dayBeforeYesterdayStr = dateFormat.format(cal.time)
            val dayBeforeYesterdayMs = cal.timeInMillis

            // Seed reports for Student 0 (Ahmad)
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[0],
                    date = dayBeforeYesterdayStr,
                    timestamp = dayBeforeYesterdayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sangat Baik",
                    homeActivity = "Membaca buku sejarah setelah Isya dan mengulang materi fisika.",
                    schoolActivity = "Aktif berdiskusi di kelas matematika, membantu mengatur bangku.",
                    notes = "Semua lancar pak.",
                    teacherComment = "Bagus sekali Ahmad, pertahankan motivasi belajarmu!"
                )
            )
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[0],
                    date = yesterdayStr,
                    timestamp = yesterdayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sehat",
                    homeActivity = "Membantu Ibu menyapu halaman rumah dan menyiram tanaman.",
                    schoolActivity = "Mengikuti olahraga basket di lapangan sekolah dan kelas bahasa Indonesia.",
                    notes = "Capek tapi seru."
                )
            )

            // Seed reports for Student 1 (Siti)
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[1],
                    date = yesterdayStr,
                    timestamp = yesterdayMs,
                    attendanceStatus = "Sakit",
                    conditionStatus = "Sakit",
                    homeActivity = "Istirahat total di kasur, minum obat paracetamol dari bidan.",
                    schoolActivity = "Tidak berangkat sekolah karena demam tinggi.",
                    notes = "Sudah kirim surat dokter ke wali kelas.",
                    teacherComment = "Semoga lekas sembuh Siti, istirahat yang cukup ya."
                )
            )

            // Seed reports for Student 2 (Budi)
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[2],
                    date = dayBeforeYesterdayStr,
                    timestamp = dayBeforeYesterdayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sehat",
                    homeActivity = "Mengerjakan tugas kelompok kimia di rumah kawan.",
                    schoolActivity = "Ujian praktek komputer di laboratorium dengan sukses.",
                    notes = "Laptop sempat kendala tapi aman."
                )
            )
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[2],
                    date = yesterdayStr,
                    timestamp = yesterdayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sangat Baik",
                    homeActivity = "Membantu Ayah memperbaiki sepeda motor setelah les.",
                    schoolActivity = "Ikut bimbingan olimpiade fisika sore hari.",
                    notes = "Paham materi vektor."
                )
            )

            // Seed reports for Student 3 (Dewi)
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[3],
                    date = yesterdayStr,
                    timestamp = yesterdayMs,
                    attendanceStatus = "Izin",
                    conditionStatus = "Sehat",
                    homeActivity = "Ikut keluarga menghadiri pernikahan sepupu di luar kota.",
                    schoolActivity = "Izin tidak masuk sekolah (surat ortu terlampir).",
                    notes = "Perjalanan pulang jam 8 malam."
                )
            )

            // Seed today's reports for a couple of students (to show some "not reported yet" status)
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[0], // Ahmad
                    date = todayStr,
                    timestamp = todayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sehat",
                    homeActivity = "Sarapan bubur ayam, berangkat sekolah naik sepeda jam 6.30.",
                    schoolActivity = "Mengerjakan presentasi kelompok PPKN hari ini.",
                    notes = "Persiapan presentasi sukses."
                )
            )

            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[2], // Budi
                    date = todayStr,
                    timestamp = todayMs,
                    attendanceStatus = "Hadir",
                    conditionStatus = "Sangat Baik",
                    homeActivity = "Membaca komik sebentar sebelum tidur malam, bangun jam 4.30 subuh.",
                    schoolActivity = "Ujian tengah semester matematika hari pertama.",
                    notes = "Bisa menjawab semua soal esai."
                )
            )
            
            dao.insertOrUpdateLaporan(
                Laporan(
                    studentId = studentIds[1], // Siti
                    date = todayStr,
                    timestamp = todayMs,
                    attendanceStatus = "Sakit",
                    conditionStatus = "Kurang Sehat",
                    homeActivity = "Mulai mendingan demamnya, makan bubur hangat.",
                    schoolActivity = "Masih izin istirahat di rumah agar benar-benar pulih.",
                    notes = "Panasnya sudah turun."
                )
            )
        }
    }
}
