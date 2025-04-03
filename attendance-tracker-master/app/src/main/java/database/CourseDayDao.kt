package com.example.attendance_tracker.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
class CourseDayDao(private val db: SQLiteDatabase) {

    fun insertCourseDay(courseDay: CourseDay): Long {
        val values = ContentValues().apply {
            put("course_id", courseDay.courseId)
            put("day_name", courseDay.dayName)
            put("date", courseDay.date)
            put("hours", courseDay.hours)
            put("attendance_taken", if (courseDay.attendanceTaken) 1 else 0)
            put("is_holiday", if (courseDay.isHoliday) 1 else 0)
            put("remark", courseDay.remark)
        }
        return db.insert("CourseDay", null, values) // ✅ Do NOT close `db`
    }

    fun getCourseDaysByCourseId(courseId: Int): List<CourseDay> {
        val cursor = db.rawQuery("SELECT * FROM CourseDay WHERE course_id = ?", arrayOf(courseId.toString()))
        val courseDays = mutableListOf<CourseDay>()

        while (cursor.moveToNext()) {
            val courseDay = CourseDay(
                dayId = cursor.getInt(0),
                courseId = cursor.getInt(1),
                dayName = cursor.getString(2),
                date = cursor.getString(3),
                hours = cursor.getInt(4),
                attendanceTaken = cursor.getInt(5) == 1,
                isHoliday = cursor.getInt(6) == 1,
                remark = if (!cursor.isNull(7)) cursor.getString(7) else null // ✅ Handle NULL remark
            )
            courseDays.add(courseDay)
        }
        cursor.close()
        return courseDays
    }

    fun setHolidayStatus(dayId: Int, isHoliday: Boolean, remark: String?) {
        val values = ContentValues().apply {
            put("is_holiday", if (isHoliday) 1 else 0)
            put("remark", remark)
        }
        db.update("CourseDay", values, "day_id = ?", arrayOf(dayId.toString()))
    }
}

