package com.example.attendance_tracker.database

data class Course(
    val courseId: Int = 0,
    val courseName: String,
    val year: Int,
    val semesterNo: Int,
    val startDate: String,
    val endDate: String,
    val hoursPerWeek: Int
)
