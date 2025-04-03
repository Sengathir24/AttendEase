package com.example.attendance_tracker.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class StudentDao(private val db: SQLiteDatabase) {

    fun insertStudent(student: Student): Long {
        val values = ContentValues().apply {
            put("day_id", student.dayId)
            put("roll_no", student.rollNo)
            put("name", student.name)
            put("present", if (student.present) 1 else 0)
            put("course_id", student.courseId)
        }
        return db.insert("Student", null, values)
    }

    fun getStudentsByDayId(dayId: Int): List<Student> {
        val students = mutableListOf<Student>()
        val cursor = db.query(
            "Student",
            null,
            "day_id = ?",
            arrayOf(dayId.toString()),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                students.add(
                    Student(
                        it.getInt(it.getColumnIndexOrThrow("day_id")),
                        it.getString(it.getColumnIndexOrThrow("roll_no")),
                        it.getString(it.getColumnIndexOrThrow("name")),
                        it.getInt(it.getColumnIndexOrThrow("present")) == 1,
                        it.getInt(it.getColumnIndexOrThrow("course_id"))
                    )
                )
            }
        }
        return students
    }
    fun markAttendanceTaken(dayId: Int) {
        val values = ContentValues()
        values.put("attendance_taken", true)

        db.update("CourseDay", values, "day_id = ?", arrayOf(dayId.toString()))
    }

    fun updateStudentAttendance(dayId: Int, rollNo: String, isPresent: Boolean) {
        val values = ContentValues().apply {
            put("present", if (isPresent) 1 else 0)
        }
        val rowsAffected = db.update(
            "Student",
            values,
            "day_id = ? AND roll_no = ?",
            arrayOf(dayId.toString(), rollNo)
        )

        Log.d("DB_UPDATE", "Updated $rowsAffected rows for RollNo: $rollNo, DayId: $dayId")
    }


    fun deleteStudent(dayId: Int, rollNo: String): Int {
        return db.delete("Student", "day_id = ? AND roll_no = ?", arrayOf(dayId.toString(), rollNo))
    }
}
