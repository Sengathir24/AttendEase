package com.example.attendance_tracker.database

data class Student(
    val dayId: Int,
    val rollNo: String,
    val name: String,
    var present: Boolean = true,  // Default value
    val courseId: Int
)

