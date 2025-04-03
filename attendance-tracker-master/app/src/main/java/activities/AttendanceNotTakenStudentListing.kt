package com.example.attendance_tracker.activities

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance_tracker.R
import com.example.attendance_tracker.adapters.StudentAdapter
import com.example.attendance_tracker.database.AppDatabase
import com.example.attendance_tracker.database.CourseDayDao
import com.example.attendance_tracker.database.Student
import com.example.attendance_tracker.database.StudentDao


class AttendanceNotTakenStudentListing : AppCompatActivity() {

    private lateinit var studentDao: StudentDao
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private var dayId: Int = 0
    private var courseId: Int = 0
    private lateinit var students: MutableList<Student> // Mutable to update list dynamically

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_listing)

        recyclerView = findViewById(R.id.recyclerView)
        confirmButton = findViewById(R.id.confirmButton)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase(this).writableDatabase
        studentDao = StudentDao(db)
        val database = AppDatabase(this).writableDatabase
        val courseDayDao = CourseDayDao(database)

        dayId = intent.getIntExtra("dayId", -1)
        courseId = intent.getIntExtra("courseId", -1)

        val courseDay = courseDayDao.getCourseDaysByCourseId(courseId).find { it.dayId == dayId }

        if (courseDay?.isHoliday == true) {
            AlertDialog.Builder(this)
                .setTitle("Holiday Notice")
                .setMessage("This day is marked as a holiday. Remark: ${courseDay.remark ?: "No remark"}")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        } else {
            if (dayId != -1 && courseId != -1) {
                loadStudents()
            }

            confirmButton.setOnClickListener {
                showAbsentStudentsDialog()
                studentDao.markAttendanceTaken(dayId)
            }
        }
    }


    private fun loadStudents() {
        students = studentDao.getStudentsByDayId(dayId).toMutableList()

        // Ensure the list is refreshed correctly
        studentAdapter = StudentAdapter(this, students) { student ->
            updateStudentInDatabase(student)
        }

        recyclerView.adapter = studentAdapter
        studentAdapter.notifyDataSetChanged() // Force RecyclerView to refresh
    }

    private fun updateStudentInDatabase(student: Student) {
        studentDao.updateStudentAttendance(student.dayId, student.rollNo, student.present)
        // Ensure data is updated in the local list to persist across scrolls
        students.find { it.rollNo == student.rollNo }?.present = student.present
    }


    private fun showAbsentStudentsDialog() {
        val absentStudents = students.filter { !it.present }
        val absentList = absentStudents.joinToString("\n") { "${it.rollNo} - ${it.name}" }

        AlertDialog.Builder(this)
            .setTitle("Absent Students")
            .setMessage(absentList.ifEmpty { "No students are absent." })
            .setPositiveButton("OK") { _, _ -> finish() }
            .create()
            .show()
    }
}

