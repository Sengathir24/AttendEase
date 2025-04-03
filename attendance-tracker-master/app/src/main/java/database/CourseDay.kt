package com.example.attendance_tracker.database

data class CourseDay(
    val dayId: Int = 0,
    val courseId: Int,
    val dayName: String,
    val date: String,
    val hours: Int,
    val attendanceTaken: Boolean,
    val isHoliday: Boolean = false,  // New field
    val remark: String? = null        // New field
)


