package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "siswa")
data class Siswa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val grade: Int, // 10, 11, 12
    val className: String, // e.g. "X MIPA 1", "XI IPS 2", "XII Bahasa"
    val nisn: String,
    val advisorName: String
)

@Entity(
    tableName = "laporan",
    foreignKeys = [
        ForeignKey(
            entity = Siswa::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class Laporan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "studentId") val studentId: Long,
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long,
    val attendanceStatus: String, // "Hadir", "Sakit", "Izin", "Alpa"
    val conditionStatus: String, // "Sangat Baik", "Sehat", "Kurang Sehat", "Sakit"
    val homeActivity: String,
    val schoolActivity: String,
    val notes: String = "",
    val teacherComment: String = ""
)

@Dao
interface SiswaDao {
    @Query("SELECT * FROM siswa ORDER BY grade ASC, className ASC, name ASC")
    fun getAllStudentsFlow(): Flow<List<Siswa>>

    @Query("SELECT * FROM siswa WHERE id = :id")
    fun getStudentByIdFlow(id: Long): Flow<Siswa?>

    @Query("SELECT * FROM siswa WHERE id = :id")
    suspend fun getStudentById(id: Long): Siswa?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(siswa: Siswa): Long

    @Update
    suspend fun updateStudent(siswa: Siswa)

    @Delete
    suspend fun deleteStudent(siswa: Siswa)

    @Query("SELECT * FROM laporan WHERE studentId = :studentId ORDER BY date DESC")
    fun getLaporanForStudentFlow(studentId: Long): Flow<List<Laporan>>

    @Query("SELECT * FROM laporan WHERE date = :date")
    fun getLaporanForDateFlow(date: String): Flow<List<Laporan>>

    @Query("SELECT * FROM laporan WHERE studentId = :studentId AND date = :date")
    suspend fun getLaporanForStudentAndDate(studentId: Long, date: String): Laporan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLaporan(laporan: Laporan): Long

    @Query("UPDATE laporan SET teacherComment = :comment WHERE id = :laporanId")
    suspend fun updateTeacherComment(laporanId: Long, comment: String)

    @Query("SELECT * FROM laporan ORDER BY date DESC")
    fun getAllLaporanFlow(): Flow<List<Laporan>>
}

@Database(entities = [Siswa::class, Laporan::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siswaDao(): SiswaDao
}
