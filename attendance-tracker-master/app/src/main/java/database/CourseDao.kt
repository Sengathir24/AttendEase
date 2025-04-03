package com.example.attendance_tracker.database

import android.content.ContentValues
import android.content.Context

class CourseDao(private val context: Context) {
    private val dbHelper = AppDatabase(context)

    fun insertCourse(course: Course): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("course_name", course.courseName)
            put("year", course.year)
            put("semester_no", course.semesterNo)
            put("start_date", course.startDate)
            put("end_date", course.endDate)
            put("hours_per_week", course.hoursPerWeek)
        }
        val result = db.insert("Course", null, values)
        db.close()
        return result
    }

    fun getAllCourses(): List<Course> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Course", null)
        val courses = mutableListOf<Course>()

        while (cursor.moveToNext()) {
            val course = Course(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getInt(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6)
            )
            courses.add(course)
        }
        cursor.close()
        db.close()
        return courses
    }
}
