package com.example.attendance_tracker.database
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE Course (
                course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                course_name TEXT NOT NULL,
                year INTEGER NOT NULL,
                semester_no INTEGER NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                hours_per_week INTEGER NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE CourseDay (
                day_id INTEGER PRIMARY KEY AUTOINCREMENT,
                course_id INTEGER NOT NULL,
                day_name TEXT NOT NULL,
                date TEXT NOT NULL,
                hours INTEGER NOT NULL,
                attendance_taken BOOLEAN DEFAULT 0,
                is_holiday BOOLEAN DEFAULT 0,  -- New field
                remark TEXT,                  -- New field
                FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
            )"""
        )

        db.execSQL(
            """CREATE TABLE Student (
                day_id INTEGER NOT NULL,
                roll_no TEXT NOT NULL,
                name TEXT NOT NULL,
                present BOOLEAN DEFAULT 1,
                course_id INTEGER NOT NULL,
                PRIMARY KEY (day_id, roll_no),
                FOREIGN KEY (day_id) REFERENCES CourseDay(day_id) ON DELETE CASCADE,
                FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE CourseDay ADD COLUMN is_holiday BOOLEAN DEFAULT 0")
            db.execSQL("ALTER TABLE CourseDay ADD COLUMN remark TEXT")
        }
    }

    companion object {
        private const val DB_NAME = "attendance.db"
        private const val DB_VERSION = 2  // Incremented version
    }
}
